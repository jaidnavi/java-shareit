package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.enumeration.StateEnum;
import ru.practicum.shareit.booking.mapping.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.enumeration.StateEnum.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDTO addBooking(BookingRequestDTO bookingDTO, Long renterId) {
        if (renterId == null) {
            throw new ValidationException("Идентификатор пользователя должен быть заполнен!");
        }

        User user = userRepository.findById(renterId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + renterId + " не найден"));

        if (bookingDTO.getItemId() == null) {
            throw new ValidationException("Идентификатор вещи должен быть заполнен!");
        }

        Item item = itemRepository.findById(bookingDTO.getItemId())
                .orElseThrow(() -> new NoDataFoundException("Вещь с id " + bookingDTO.getItemId() + " не найдена"));

        if (item.getUser() != null && item.getUser().getId().equals(renterId)) {
            throw new NoDataFoundException("Нельзя бронировать собственную вещь");
        }

        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь не доступна для бронирования!");
        }

        LocalDateTime start = bookingDTO.getStart();
        LocalDateTime end = bookingDTO.getEnd();
        if (start == null || end == null || !start.isBefore(end)) {
            throw new ValidationException("Некорректные даты бронирования");
        }

        Booking booking = bookingMapper.toBooking(bookingDTO, item, user);
        booking.setStatus(BookingStatus.WAITING.name());
        return bookingMapper.toBookingDTO(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDTO confirmOrReject(Long bookingId, Long itemOwnerId, Boolean approved) {
        if (bookingId == null) {
            throw new ValidationException("Идентификатор бронирования должен быть заполнен!");
        }
        if (itemOwnerId == null) {
            throw new ValidationException("Идентификатор владельца должен быть заполнен!");
        }
        if (approved == null) {
            throw new ValidationException("Параметр approved должен быть заполнен!");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoDataFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!itemOwnerId.equals(booking.getItem().getUser().getId())) {
            throw new ValidationException(
                    "Подтверждение или отклонение запроса на бронирование может быть выполнено только владельцем вещи.");
        }

        if (!BookingStatus.WAITING.name().equals(booking.getStatus())) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED.name() : BookingStatus.REJECTED.name());
        return bookingMapper.toBookingDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDTO getBookingById(Long bookingId, Long userId) {
        if (bookingId == null) {
            throw new ValidationException("Идентификатор бронирования должен быть заполнен!");
        }
        if (userId == null) {
            throw new ValidationException("Идентификатор пользователя должен быть заполнен!");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoDataFoundException("Бронирование с id " + bookingId + " не найдено"));

        boolean isOwner = userId.equals(booking.getItem().getUser().getId());
        boolean isRenter = userId.equals(booking.getUser().getId());
        if (!isOwner && !isRenter) {
            throw new NoDataFoundException(
                    "Запрос может быть выполнен либо автором бронирования, либо владельцем вещи");
        }
        return bookingMapper.toBookingDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getAllBookingByUserId(Long currentUserId, StateEnum state) {
        validateStateAndUser(currentUserId, state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByUserIdOrderByBookingStartDesc(currentUserId);
            case PAST -> bookingRepository.findAllByUserIdAndState(
                    currentUserId, BookingStatus.APPROVED.name(), PAST.name(), now);
            case CURRENT -> bookingRepository.findAllByUserIdAndState(
                    currentUserId, BookingStatus.APPROVED.name(), CURRENT.name(), now);
            case FUTURE -> bookingRepository.findAllByUserIdAndState(
                    currentUserId, BookingStatus.APPROVED.name(), FUTURE.name(), now);
            case WAITING -> bookingRepository.findAllByUserIdAndState(
                    currentUserId, BookingStatus.WAITING.name(), WAITING.name(), now);
            case REJECTED -> bookingRepository.findAllByUserIdAndState(
                    currentUserId, BookingStatus.REJECTED.name(), REJECTED.name(), now);
        };

        return bookings.stream().map(bookingMapper::toBookingDTO).toList();
    }

    @Override
    public List<BookingResponseDTO> getAllBookingByOwnerId(Long currentUserId, StateEnum state) {
        validateStateAndUser(currentUserId, state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByItemUserIdOrderByBookingStartDesc(currentUserId);
            case PAST -> bookingRepository.findAllByOwnerIdAndState(
                    currentUserId, BookingStatus.APPROVED.name(), PAST.name(), now);
            case CURRENT -> bookingRepository.findAllByOwnerIdAndState(
                    currentUserId, BookingStatus.APPROVED.name(), CURRENT.name(), now);
            case FUTURE -> bookingRepository.findAllByOwnerIdAndState(
                    currentUserId, BookingStatus.APPROVED.name(), FUTURE.name(), now);
            case WAITING -> bookingRepository.findAllByOwnerIdAndState(
                    currentUserId, BookingStatus.WAITING.name(), WAITING.name(), now);
            case REJECTED -> bookingRepository.findAllByOwnerIdAndState(
                    currentUserId, BookingStatus.REJECTED.name(), REJECTED.name(), now);
        };

        return bookings.stream().map(bookingMapper::toBookingDTO).toList();
    }

    private void validateStateAndUser(Long userId, StateEnum state) {
        if (state == null) {
            throw new ValidationException("Параметр статуса бронирования должен быть заполнен!");
        }
        if (userId == null) {
            throw new ValidationException("Идентификатор пользователя должен быть заполнен!");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + userId + " не найден"));
    }
}