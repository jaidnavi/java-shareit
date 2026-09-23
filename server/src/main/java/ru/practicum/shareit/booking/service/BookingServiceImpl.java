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
import java.util.Objects;

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

        User user = chekUser(renterId);
        Item item = chekItem(bookingDTO.getItemId(), renterId);
        chekNewBooking(bookingDTO);

        Booking booking = bookingMapper.toBooking(bookingDTO, item, user);
        booking.setStatus(BookingStatus.WAITING.name());
        return bookingMapper.toBookingDTO(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDTO confirmOrReject(Long bookingId, Long itemOwnerId, Boolean approved) {

        chekParamIsNotNull(bookingId, itemOwnerId, approved);
        Booking booking = checkBooking(bookingId);
        checkOwnerForConfirmOrReject(itemOwnerId, booking.getItem().getUser().getId());
        checkStatus(booking.getStatus());

        booking.setStatus(approved ? BookingStatus.APPROVED.name() : BookingStatus.REJECTED.name());
        return bookingMapper.toBookingDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDTO getBookingById(Long bookingId, Long userId) {

        chekUser(userId);
        Booking booking = checkBooking(bookingId);
        checkOwnerForGetBooking(userId, booking.getItem().getUser().getId(), booking.getUser().getId());

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

    private User chekUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("Идентификатор пользователя должен быть заполнен!");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + userId + " не найден"));
    }


    private Item chekItem(Long itemId, Long renterId) {
        if (itemId == null) {
            throw new ValidationException("Идентификатор вещи должен быть заполнен!");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoDataFoundException("Вещь с id " + itemId + " не найдена"));

        if (item.getUser() != null && item.getUser().getId().equals(renterId)) {
            throw new NoDataFoundException("Нельзя бронировать собственную вещь");
        }

        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь не доступна для бронирования!");
        }

        return item;
    }

    private void chekNewBooking(BookingRequestDTO bookingDTO) {

        LocalDateTime start = bookingDTO.getStart();
        LocalDateTime end = bookingDTO.getEnd();

        if (start == null || end == null || !start.isBefore(end)) {
            throw new ValidationException("Некорректные даты бронирования");
        }
    }

    private void chekParamIsNotNull(Long bookingId, Long itemOwnerId, Boolean approved) {
        if (bookingId == null) {
            throw new ValidationException("Идентификатор бронирования должен быть заполнен!");
        }
        if (itemOwnerId == null) {
            throw new ValidationException("Идентификатор владельца должен быть заполнен!");
        }
        if (approved == null) {
            throw new ValidationException("Параметр approved должен быть заполнен!");
        }

    }

    private Booking checkBooking(Long bookingId) {
        if (bookingId == null) {
            throw new ValidationException("Идентификатор бронирования должен быть заполнен!");
        }

        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoDataFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

    private void checkOwnerForConfirmOrReject(Long itemOwnerId, Long userId) {
        if (!Objects.equals(itemOwnerId, userId)) {
            throw new ValidationException(
                    "Подтверждение или отклонение запроса на бронирование может быть выполнено только владельцем вещи.");
        }
    }

    private void checkStatus(String status) {
        if (!BookingStatus.WAITING.name().equals(status)) {
            throw new ValidationException("Бронирование уже обработано");
        }
    }

    private void checkOwnerForGetBooking(Long userId, Long ownerId, Long rentarId) {
        boolean isOwner = userId.equals(ownerId);
        boolean isRenter = userId.equals(rentarId);
        if (!isOwner && !isRenter) {
            throw new NoDataFoundException(
                    "Запрос может быть выполнен либо автором бронирования, либо владельцем вещи");
        }
    }

}