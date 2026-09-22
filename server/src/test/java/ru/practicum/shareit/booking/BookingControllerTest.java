package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private BookingRequestDTO requestDto;
    private BookingResponseDTO responseDto;
    private final Long userId = 1L;
    private final Long bookingId = 100L;

    @BeforeEach
    void setUp() {
        requestDto = BookingRequestDTO.builder()
                .itemId(5L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        responseDto = BookingResponseDTO.builder()
                .id(bookingId)
                .status("WAITING")
                .build();
    }

    @Test
    void testAddNewBooking() throws Exception {
        Mockito.when(bookingService.addBooking(any(BookingRequestDTO.class), eq(userId)))
                .thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        Mockito.verify(bookingService, Mockito.times(1)).addBooking(any(BookingRequestDTO.class), eq(userId));
    }

    @Test
    void testConfirmReject() throws Exception {
        responseDto.setStatus("APPROVED");
        Mockito.when(bookingService.confirmOrReject(eq(bookingId), eq(userId), eq(true)))
                .thenReturn(responseDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Mockito.verify(bookingService, Mockito.times(1)).confirmOrReject(bookingId, userId, true);
    }

    @Test
    void testGetBookingById() throws Exception {
        Mockito.when(bookingService.getBookingById(eq(bookingId), eq(userId)))
                .thenReturn(responseDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));

        Mockito.verify(bookingService, Mockito.times(1)).getBookingById(bookingId, userId);
    }

    @Test
    void testGetAllBookingByUserIdWithValidState() throws Exception {
        Mockito.when(bookingService.getAllBookingByUserId(eq(userId), any()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING") // Валидное состояние
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void testGetAllBookingByUserIdWithInvalidState() {
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mvc.perform(get("/bookings")
                    .header("X-Sharer-User-Id", userId)
                    .param("state", "INVALID_STATE_NAME")
                    .accept(MediaType.APPLICATION_JSON)
            );
        });
    }

    @Test
    void testGetAllBookingByOwnerId() throws Exception {
        Mockito.when(bookingService.getAllBookingByOwnerId(eq(userId), any()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));

        Mockito.verify(bookingService, Mockito.times(1)).getAllBookingByOwnerId(eq(userId), any());
    }

    @Test
    void testGetAllBookingByOwnerIdWithInvalidState() {
        ServletException exception = Assertions.assertThrows(
                jakarta.servlet.ServletException.class, () -> {
                    mvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", userId)
                            .param("state", "UNSUPPORTED_STATUS")
                            .accept(MediaType.APPLICATION_JSON)
                    );
                }
        );

        Throwable rootCause = exception.getCause();

        Assertions.assertInstanceOf(IllegalArgumentException.class, rootCause);

        Assertions.assertEquals(
                "Неверное значение параметра state: UNSUPPORTED_STATUS",
                rootCause.getMessage()
        );
    }

}