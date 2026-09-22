package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

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
    public ItemDTO getItem(@Valid @PathVariable Long itemId,
                           @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemService.getItem(itemId, ownerId);
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

    @PostMapping("/{itemId}/comment")
    public CommentsDTO addCommentToItem(@PathVariable Long itemId,
                                        @Valid @RequestBody CommentsDTO commentsDTO,
                                        @RequestHeader(X_SHARER_USER_ID) Long authorId) {
        return itemService.addCommentToItem(itemId, commentsDTO, authorId);
    }

}
