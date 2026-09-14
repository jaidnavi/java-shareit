package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;

import ru.practicum.shareit.booking.enumeration.StateEnum;

import java.util.List;


public interface BookingService {
    BookingResponseDTO addBooking(BookingRequestDTO bookingDTO, Long renterId);

    BookingResponseDTO confirmOrReject(Long bookingId, Long itemOwnerId, Boolean approved);

    BookingResponseDTO getBookingById(Long bookingId, Long userId);

    List<BookingResponseDTO> getAllBookingByUserId(Long currentUserId, StateEnum state);

    List<BookingResponseDTO> getAllBookingByOwnerId(Long currentUserId, StateEnum state);

}
