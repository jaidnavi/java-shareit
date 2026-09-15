package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByUserId(long ownerId);

    @Query("SELECT it " +
            "FROM Item AS it " +
            "WHERE it.available = TRUE " +
            "  AND (UPPER(it.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "   OR UPPER(it.description) LIKE UPPER(CONCAT('%', :text, '%')))")
    List<Item> findByNameDescription(@Param("text") String text);
}