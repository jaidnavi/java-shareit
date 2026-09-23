package ru.practicum.shareit.item.service;


import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.item.dto.ItemDTO;

import java.util.Collection;

/**
 * Интерфейс функционала для объекта вещи
 */
public interface ItemService {

    /**
     * Метод создания объекта вещь
     *
     * @param itemDTO данные о вещи
     * @param ownerId идентификатор пользователя - владельца
     * @return созданный объект вещи
     */
    ItemDTO insertItem(ItemDTO itemDTO, Long ownerId);

    /**
     * Метод обновления объекта вещь
     *
     * @param itemId  идентификатор вещи
     * @param itemDTO данные о вещи
     * @param ownerId идентификатор пользователя - владельца
     * @return измененный объект вещи
     */
    ItemDTO updateItem(Long itemId, ItemDTO itemDTO, Long ownerId);

    /**
     * Метод получения объекта вещь
     *
     * @param itemId  идентификатор вещи
     * @param ownerId идентификатор пользователя - владельца
     * @return объект вещи
     */
    ItemDTO getItem(Long itemId, Long ownerId);

    /**
     * Метод получения списка вещей по владельцу
     *
     * @param ownerId идентификатор пользователя - владельца
     * @return список вещи
     */
    Collection<ItemDTO> getListItemByOwner(Long ownerId);

    /**
     * Метод получения списка вещей по названию или описанию
     *
     * @param searchText текст поиска
     * @return список вещи
     */
    Collection<ItemDTO> searchItemBySearchText(String searchText);

    /**
     * Метод добавления отзыва к вещи
     *
     * @param itemId   идентификатор вещи
     * @param comment  текст отзыва
     * @param authorId владелец отзыва
     * @return список вещи
     */
    CommentsDTO addCommentToItem(Long itemId, CommentsDTO comment, Long authorId);
}
