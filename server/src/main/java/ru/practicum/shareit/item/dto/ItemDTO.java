package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.comment.dto.CommentsDTO;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.constants.Constants.JSON_DATE_TIME_PATTERN;

@Data
@Builder
@AllArgsConstructor
public class ItemDTO {

    private Long id;
    @NotNull(message = "Наименование предмета не может быть пустым")
    @NotBlank(message = "Наименование предмета не может быть пустым")
    private String name;
    @NotNull(message = "Описание предмета не может быть пустым")
    @NotBlank(message = "Описание предмета не может быть пустым")
    private String description;

    @NotNull(message = "Должна быть указана доступность предмета")
    private Boolean available;


    private List<CommentsDTO> comments;

    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime lastBooking;

    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime nextBooking;

    private Long requestId;

}