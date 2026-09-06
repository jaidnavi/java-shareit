package ru.practicum.shareit.item.mapping;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class ItemMapper {

    public Item itemDTOToItem(ItemDTO itemDTO) {
        return Item.builder()
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .available(itemDTO.getAvailable())
                .build();
    }

    public ItemDTO itemToItemDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public Collection<ItemDTO> itemToItemDTOCollection(Collection<Item> items) {
        if (items == null) {
            return null;
        }

        Collection<ItemDTO> collection = new ArrayList<>(items.size());
        for (Item item : items) {
            collection.add(itemToItemDTO(item));
        }
        return collection;
    }

}
