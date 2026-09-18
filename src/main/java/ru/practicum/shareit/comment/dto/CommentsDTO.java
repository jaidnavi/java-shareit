package ru.practicum.shareit.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import static ru.practicum.shareit.constants.Constants.JSON_DATE_TIME_PATTERN;

@Data
public class CommentsDTO {

    private Long id;
    @NotNull(message = "Текст отзыва не может быть пустым")
    private String text;
    private String authorName;
    @JsonFormat(pattern = JSON_DATE_TIME_PATTERN)
    private LocalDateTime created;
}
