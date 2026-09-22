package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentsDTO;
import ru.practicum.shareit.item.dto.ItemDTO;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@Valid @RequestBody ItemDTO newItem,
                                      @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemClient.insertItem(newItem, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable Long itemId,
                                         @RequestBody ItemDTO itemDTO,
                                         @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        log.info("update");
        return itemClient.updateItem(itemId, itemDTO, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@Valid @PathVariable Long itemId,
                                          @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemClient.getItem(itemId, ownerId);
    }

    @GetMapping
    public ResponseEntity<Object> getListItemByOwner(@RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemClient.getListItemByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemBySearchText(
            @RequestParam String text,
            @RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemClient.searchItemBySearchText(text, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addCommentToItem(@PathVariable Long itemId,
                                                   @Valid @RequestBody CommentsDTO commentsDTO,
                                                   @RequestHeader(X_SHARER_USER_ID) Long authorId) {
        return itemClient.addCommentToItem(itemId, commentsDTO, authorId);
    }
}
