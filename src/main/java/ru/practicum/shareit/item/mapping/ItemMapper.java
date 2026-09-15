package ru.practicum.shareit.item.mapping;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapper {

    public Item itemDTOToItem(ItemDTO itemDTO) {
        Item item = new Item();
        if (itemDTO.getName() != null) {
            item.setName(itemDTO.getName());
        }
        if (itemDTO.getDescription() != null) {
            item.setDescription(itemDTO.getDescription());
        }
        if (itemDTO.getAvailable() != null) {
            item.setAvailable(itemDTO.getAvailable());
        }
        return item;
    }

    public ItemDTO itemToItemDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

}
