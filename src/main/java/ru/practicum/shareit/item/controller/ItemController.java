package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDTO add(@Valid @RequestBody ItemDTO newItem,
                       @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemService.insertItem(newItem, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDTO update(@PathVariable Long itemId,
                          @RequestBody ItemDTO itemDTO,
                          @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        log.info("update");
        return itemService.updateItem(itemId, itemDTO, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDTO getItem(@Valid @PathVariable Long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping
    public Collection<ItemDTO> getListItemByOwner(@RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemService.getListItemByOwner(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemDTO> searchItemBySearchText(
            @RequestParam String text) {
        return itemService.searchItemBySearchText(text);
    }

}
