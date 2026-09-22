package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.enumeration.StateEnum;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;

@RestController
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDTO addNewBooking(@Valid @RequestBody BookingRequestDTO newBookingDto,
                                            @RequestHeader(X_SHARER_USER_ID) Long renterId) {
        return bookingService.addBooking(newBookingDto, renterId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDTO confirmReject(@Valid @PathVariable Long bookingId,
                                            @RequestHeader(X_SHARER_USER_ID) Long itemOwnerId,
                                            @RequestParam Boolean approved) {
        return bookingService.confirmOrReject(bookingId, itemOwnerId, approved);
    }


    @GetMapping
    public List<BookingResponseDTO> getAllBookingByUserId(@RequestHeader(X_SHARER_USER_ID) Long currentUserId,
                                                          @RequestParam(defaultValue = "ALL") String state) {
        try {
            StateEnum stateEnum = StateEnum.valueOf(state.toUpperCase());
            return bookingService.getAllBookingByUserId(currentUserId, stateEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверное значение параметра state: " + state.toUpperCase());
        }
    }


    @GetMapping("/owner")
    public List<BookingResponseDTO> getAllBookingByOwnerId(@RequestHeader(X_SHARER_USER_ID) Long currentUserId,
                                                           @RequestParam(defaultValue = "ALL") String state) {
        try {
            StateEnum stateEnum = StateEnum.valueOf(state.toUpperCase());
            return bookingService.getAllBookingByOwnerId(currentUserId, stateEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверное значение параметра state: " + state.toUpperCase());
        }
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDTO getBookingById(@Valid @PathVariable Long bookingId,
                                             @RequestHeader(X_SHARER_USER_ID) Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }


}
