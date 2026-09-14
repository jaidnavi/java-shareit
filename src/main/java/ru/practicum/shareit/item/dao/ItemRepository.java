package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByUserId(long ownerId);

    @Query("SELECT it " +
            "FROM Item AS it " +
            "WHERE it.available = TRUE " +
            "    AND (upper(it.name) LIKE upper(?1) " +
            "        OR upper(it.description) LIKE upper(?2))")
    List<Item> findByNameDescription(String name, String description);
}
