package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemDAO {
    Item insert(Item item);

    Item update(Item item);

    Optional<Item> get(Long itemId);

    Collection<Item> searchByOwner(Long ownerId);

    Collection<Item> searchBySearchText(String searchText);

}
