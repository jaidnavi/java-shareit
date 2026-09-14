package ru.practicum.shareit.item.service;


import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.item.dto.ItemDTO;

import java.util.Collection;

public interface ItemService {
    ItemDTO insertItem(ItemDTO itemDTO, Long ownerId);

    ItemDTO updateItem(Long itemId, ItemDTO itemDTO, Long ownerId);

    ItemDTO getItem(Long itemId, Long ownerId);

    Collection<ItemDTO> getListItemByOwner(Long ownerId);

    Collection<ItemDTO> searchItemBySearchText(String searchText);

    CommentsDTO addCommentToItem(Long itemId, CommentsDTO comment, Long authorId);
}
