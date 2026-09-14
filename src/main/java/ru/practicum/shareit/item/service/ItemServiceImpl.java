package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.enumeration.StateEnum;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.dao.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.comment.mapping.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;

    @Autowired
    public ItemServiceImpl(ItemMapper itemMapper, ItemRepository itemRepository, UserRepository userRepository, CommentRepository commentRepository, CommentMapper commentMapper, BookingRepository bookingRepository) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public ItemDTO insertItem(ItemDTO itemDTO, Long ownerId) {
        if (ownerId == null) {
            throw new ValidationException("При создании предмета не указан его владелец");
        }

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));

        Item item = itemMapper.itemDTOToItem(itemDTO);
        item.setUser(userRepository.findById(ownerId).get());


        Item savedItem = itemRepository.save(item);

        return itemMapper.itemToItemDTO(savedItem);
    }

    @Override
    @Transactional
    public ItemDTO updateItem(Long itemId, ItemDTO itemDTO, Long ownerId) {

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoDataFoundException("Вещь с id " + itemId + " не найдена!"));

        if (!Objects.equals(ownerId, item.getUser().getId())) {
            throw new ValidationException("Указанный пользователь не является владельцем его вещи. Редактировать вещь может только её владелец.");
        }

        if (itemDTO.getName() != null) {
            item.setName(itemDTO.getName());
        }
        if (itemDTO.getDescription() != null) {
            item.setDescription(itemDTO.getDescription());
        }
        if (itemDTO.getAvailable() != null) {
            item.setAvailable(itemDTO.getAvailable());
        }
        return itemMapper.itemToItemDTO(itemRepository.save(item));
    }

    @Override
    public ItemDTO getItem(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoDataFoundException("Предмет с id " + itemId + " не найден"));

        ItemDTO itemDTO = itemMapper.itemToItemDTO(item);

        List<Comment> comments = commentRepository.findByItemId(itemId);

        if (!comments.isEmpty()) {
            itemDTO.setComments(comments.stream()
                    .map(commentMapper::toCommentsDTO)
                    .toList());
        }

        if (item.getUser().getId().equals(ownerId)) {
            itemDTO.setLastBooking(bookingRepository.findLastBookingDatetime(itemId));
            itemDTO.setNextBooking(bookingRepository.findNextBookingDatetime(itemId));
        }

        return itemDTO;
    }

    private ItemDTO makeItemWithCommentsDto(Item item, List<Comment> comments) {
        ItemDTO itemDTO = itemMapper.itemToItemDTO(item);
        itemDTO.setComments(comments.stream()
                .map(commentMapper::toCommentsDTO)
                .toList());
        return itemDTO;
    }

    @Override
    public Collection<ItemDTO> getListItemByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));

        Map<Long, Item> itemMap = itemRepository.findAllByUserId(ownerId)
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        Map<Long, List<Comment>> commentMap = commentRepository.findByItemIdIn(itemMap.keySet()).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return itemMap.values()
                .stream()
                .map(item -> makeItemWithCommentsDto(item,
                        commentMap.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

    }

    @Override
    public Collection<ItemDTO> searchItemBySearchText(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return List.of();
        }

        return itemRepository.findByNameDescription(searchText, searchText).stream()
                .map(itemMapper::itemToItemDTO)
                .toList();
    }

    @Override
    @Transactional
    public CommentsDTO addCommentToItem(Long itemId, CommentsDTO commentsDTO, Long authorId) {

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new NoDataFoundException("Автор с id " + authorId + " не найден "));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoDataFoundException("Вещь с id " + itemId + " не найдена"));
        itemAvailableCheck(item);
        checkUserBookings(authorId);
        Comment comment = commentMapper.toComment(commentsDTO, item, user);
        return commentMapper.toCommentsDTO(commentRepository.save(comment));
    }

    private static void itemAvailableCheck(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования!");
        }
    }

    private void checkUserBookings(Long authorId) {
        List<Booking> bookings = bookingRepository.findAllByUserIdAndState(authorId, BookingStatus.APPROVED.name(),
                StateEnum.PAST.name(), LocalDateTime.now());
        log.trace("bookings.size={}", bookings.size());
        if (bookings.isEmpty()) {
            throw new ValidationException("Отзыв может оставить только тот пользователь, " +
                    "который брал эту вещь в аренду, и только после окончания срока аренды!");
        }
    }

}
