package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDTO;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDTO addNewRequest(ItemRequestDTO itemRequestDTO, Long userId);


    List<ItemRequestDTO> getItemRequests(Long ownerId, boolean all);


    ItemRequestDTO getItemRequestById(Long requestId);
}
