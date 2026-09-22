package ru.practicum.shareit.request.mapping;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDTO;
import ru.practicum.shareit.request.dto.RequestedItemDTO;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemRequestMapper {

    public static ItemRequest itemRequestDTOToItemRequest(ItemRequestDTO itemRequestDTO) {
        ItemRequest itemRequest = new ItemRequest();
        if (itemRequestDTO.getDescription() != null) {
            itemRequest.setDescription(itemRequestDTO.getDescription());
        }
        return itemRequest;
    }

    public static ItemRequestDTO itemRequestToItemRequestDTO(ItemRequest itemRequest, List<Item> items) {
        List<RequestedItemDTO> listRequestItemDTO = new ArrayList<>();
        if (items != null && !items.isEmpty()) {
            listRequestItemDTO = items.stream()
                    .filter(item -> item.getRequestId().equals(itemRequest.getId()))
                    .map(item -> RequestedItemDTO.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .ownerId(item.getUser().getId())
                            .build())
                    .toList();
        }


        return ItemRequestDTO.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreationDate())
                .items(listRequestItemDTO)
                .build();
    }
}
