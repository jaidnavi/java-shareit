package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDTO;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;


@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDTO addNewRequest(@Valid @RequestBody ItemRequestDTO newRequest,
                                        @RequestHeader(X_SHARER_USER_ID) Long authorId) {
        return itemRequestService.addNewRequest(newRequest, authorId);
    }

    @GetMapping
    public List<ItemRequestDTO> getItemRequests(@RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemRequestService.getItemRequests(ownerId, false);
    }

    @GetMapping("/all")
    public List<ItemRequestDTO> getAllItemRequests(@RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemRequestService.getItemRequests(ownerId, true);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDTO getItemRequestById(@RequestHeader(X_SHARER_USER_ID) Long authorId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(requestId);
    }
}
