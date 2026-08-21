package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemDAO;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDAO;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemDAO itemDAO;
    private final ItemMapper itemMapper;
    private final UserDAO userDAO;

    @Autowired
    public ItemServiceImpl(ItemDAO itemDAO, ItemMapper itemMapper, UserDAO userDAO) {
        this.itemDAO = itemDAO;
        this.itemMapper = itemMapper;
        this.userDAO = userDAO;
    }

    @Override
    public ItemDTO insertItem(ItemDTO itemDTO, Long ownerId) {
        if (ownerId == null) {
            throw new ValidationException("При создании предмета не указан его владелец");
        }

        userDAO.get(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));

        Item item = itemMapper.itemDTOToItem(itemDTO);
        item.setOwnerId(ownerId);

        Item savedItem = itemDAO.insert(item);

        return itemMapper.itemToItemDTO(savedItem);
    }

    @Override
    public ItemDTO updateItem(Long itemId, ItemDTO itemDTO, Long ownerId) {

        userDAO.get(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));

        Item item = itemMapper.itemDTOToItem(itemDTO);

        item.setId(itemId);
        item.setOwnerId(ownerId);

        Item updatedItem = itemDAO.update(item);

        return itemMapper.itemToItemDTO(updatedItem);
    }

    @Override
    public ItemDTO getItem(Long itemId) {
        Item item = itemDAO.get(itemId)
                .orElseThrow(() -> new NoDataFoundException("Предмет с id " + itemId + " не найден"));

        return itemMapper.itemToItemDTO(item);
    }

    @Override
    public Collection<ItemDTO> getListItemByOwner(Long ownerId) {
        userDAO.get(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));
        return itemMapper.itemToItemDTOCollection(itemDAO.searchByOwner(ownerId));
    }

    @Override
    public Collection<ItemDTO> searchItemBySearchText(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return List.of();
        }

        return itemMapper.itemToItemDTOCollection(itemDAO.searchBySearchText(searchText));
    }
}
