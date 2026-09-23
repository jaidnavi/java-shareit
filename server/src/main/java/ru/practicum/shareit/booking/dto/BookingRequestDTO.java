package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static ru.practicum.shareit.constants.Constants.JSON_DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "Идентификатор бронируемой вещи не может быть пустым")
    private Long itemId;

    @NotNull(message = "Дата и время начала бронирования не может быть пустой")
    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime start;

    @NotNull(message = "Дата и время окончания бронирования не может быть пустой")
    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime end;
}
