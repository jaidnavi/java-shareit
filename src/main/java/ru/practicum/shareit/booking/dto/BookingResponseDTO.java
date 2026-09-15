package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;

import static ru.practicum.shareit.constants.Constants.JSON_DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private Long itemId;
    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime start;
    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime end;
    private ItemDTO item;
    private UserDTO booker;
    private String status;
}
