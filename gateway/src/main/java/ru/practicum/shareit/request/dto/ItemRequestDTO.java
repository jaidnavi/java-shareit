package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.constants.Constants.JSON_DATE_TIME_PATTERN;

@Data
@Builder
public class ItemRequestDTO {

    private Long id;
    @NotNull(message = "Описание должно быть указано")
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime created;
    private List<RequestedItemDTO> items;
}
