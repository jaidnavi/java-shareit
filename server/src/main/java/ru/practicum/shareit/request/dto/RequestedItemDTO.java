package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RequestedItemDTO {
    private Long id;
    private String name;
    private Long ownerId;
}
