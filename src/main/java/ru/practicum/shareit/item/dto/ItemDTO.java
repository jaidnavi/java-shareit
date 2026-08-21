package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
}