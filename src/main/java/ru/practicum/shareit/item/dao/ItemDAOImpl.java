package ru.practicum.shareit.item.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemDAOImpl implements ItemDAO {

    private final Map<Long, Item> items = new HashMap<>();

    private long getNextItemId() {
        long currItemId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        currItemId++;
        return currItemId;
    }

    @Override
    public Item insert(Item item) {
        item.setId(getNextItemId());
        items.put(item.getId(), item);
        log.info("Предмет {} добавлен (id = {}).", item.getName(), item.getId());
        return item;
    }

    @Override
    public Item update(Item item) {
        Item updatedItem = items.get(item.getId());
        if (updatedItem == null) {
            throw new NoDataFoundException("Не найден предмет с идентификатором " + item.getId());
        }

        if (item.getName() != null && !item.getName().isBlank()) {
            updatedItem.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }

        log.info("Предмет с id = {} успешно обновлен.", item.getId());
        return updatedItem;
    }

    @Override
    public Optional<Item> get(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Collection<Item> searchByOwner(Long ownerId) {
        if (ownerId == null) {
            return List.of();
        }

        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> searchBySearchText(String searchText) {

        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }

        String lowerSearchText = searchText.toLowerCase();

        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> {
                    boolean isFindName = item.getName() != null
                            && item.getName().toLowerCase().contains(lowerSearchText);
                    boolean isFindDescription = item.getDescription() != null
                            && item.getDescription().toLowerCase().contains(lowerSearchText);
                    return isFindName || isFindDescription;
                })
                .collect(Collectors.toList());
    }
}
