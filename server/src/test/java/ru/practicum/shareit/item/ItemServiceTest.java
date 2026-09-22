package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.comment.dao.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.comment.mapping.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
@Transactional
@SpringBootTest()
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    ItemRepository itemRepositoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @Mock
    CommentRepository commentRepositoryMock;
    @Mock
    BookingRepository bookingRepositoryMock;

    private User testUser1;
    private Item testItem1;
    private Item testItem2;
    private final ItemService service;
    private final ItemMapper itemMapper;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;

    @BeforeEach
    void settingBeforeEach() {

        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setName("Роман");
        testUser1.setEmail("kosticin@test.ru");

        testItem1 = new Item();
        testItem1.setName("Вещь 1");
        testItem1.setDescription("Вещь 1");
        testItem1.setId(1L);
        testItem1.setUser(testUser1);

        testItem2 = new Item();
        testItem2.setId(2L);
        testItem2.setName("Вещь 2");
        testItem2.setDescription("Вещь 2");
        testItem2.setUser(testUser1);

    }

    @Test
    public void testGetListItemByOwnerCorrect() {

        ItemService itemService = new ItemServiceImpl(itemMapper, itemRepositoryMock, userRepositoryMock, commentRepositoryMock, commentMapper, bookingRepositoryMock);

        Mockito.when(userRepositoryMock.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(testUser1));

        Mockito.when(itemRepositoryMock.findAllByUserId(Mockito.anyLong()))
                .thenReturn(List.of(testItem1, testItem2));

        Collection<ItemDTO> listItemDTO = itemService.getListItemByOwner(99999L);
        Assertions.assertEquals(2, listItemDTO.size());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findAllByUserId(Mockito.anyLong());

    }


    @Test
    public void testSearchItemBySearchTextCorrect() {

        ItemService itemService = new ItemServiceImpl(itemMapper, itemRepositoryMock, userRepositoryMock, commentRepositoryMock, commentMapper, bookingRepositoryMock);

        Mockito.when(itemRepositoryMock.findByNameDescription(Mockito.anyString()))
                .thenReturn(List.of(testItem1, testItem2));

        Collection<ItemDTO> listItemDTO = itemService.searchItemBySearchText("поиск");
        Assertions.assertEquals(2, listItemDTO.size());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findByNameDescription(Mockito.anyString());

    }

    @Test
    public void testInsertItemCorrect() {
        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        testItem1.setAvailable(true);
        ItemDTO inputDto = itemMapper.itemToItemDTO(testItem1);

        Mockito.when(userRepositoryMock.findById(testUser1.getId()))
                .thenReturn(Optional.of(testUser1));

        Mockito.when(itemRepositoryMock.save(Mockito.any(Item.class)))
                .thenReturn(testItem1);

        ItemDTO savedItemDto = itemService.insertItem(inputDto, testUser1.getId());

        Assertions.assertNotNull(savedItemDto);
        Assertions.assertEquals(testItem1.getId(), savedItemDto.getId());
        Assertions.assertEquals(testItem1.getName(), savedItemDto.getName());
        Assertions.assertEquals(testItem1.getDescription(), savedItemDto.getDescription());
        Assertions.assertEquals(testItem1.getAvailable(), savedItemDto.getAvailable());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).save(Mockito.any(Item.class));
    }

    @Test
    public void testUpdateItemCorrect() {
        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        ItemDTO updateDto = ItemDTO.builder()
                .name("Обновленная вещь 1")
                .description("Обновленное описание 1")
                .available(false)
                .build();

        Item updatedItem = new Item();
        updatedItem.setId(testItem1.getId());
        updatedItem.setName(updateDto.getName());
        updatedItem.setDescription(updateDto.getDescription());
        updatedItem.setAvailable(updateDto.getAvailable());
        updatedItem.setUser(testUser1);


        Mockito.when(userRepositoryMock.findById(testUser1.getId()))
                .thenReturn(Optional.of(testUser1));

        Mockito.when(itemRepositoryMock.findById(testItem1.getId()))
                .thenReturn(Optional.of(testItem1));

        Mockito.when(itemRepositoryMock.save(Mockito.any(Item.class)))
                .thenReturn(updatedItem);

        ItemDTO resultDto = itemService.updateItem(testItem1.getId(), updateDto, testUser1.getId());

        Assertions.assertNotNull(resultDto);
        Assertions.assertEquals(testItem1.getId(), resultDto.getId());
        Assertions.assertEquals("Обновленная вещь 1", resultDto.getName());
        Assertions.assertEquals("Обновленное описание 1", resultDto.getDescription());
        Assertions.assertEquals(false, resultDto.getAvailable());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).save(Mockito.any(Item.class));

    }

    @Test
    public void testGetItemCorrectForOwner() {

        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );


        Mockito.when(itemRepositoryMock.findById(testItem1.getId()))
                .thenReturn(Optional.of(testItem1));

        Mockito.when(commentRepositoryMock.findByItemId(testItem1.getId()))
                .thenReturn(List.of());

        Mockito.when(bookingRepositoryMock.findLastBookingDatetime(testItem1.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepositoryMock.findNextBookingDatetime(testItem1.getId()))
                .thenReturn(null);

        ItemDTO resultDto = itemService.getItem(testItem1.getId(), testUser1.getId());

        Assertions.assertNotNull(resultDto);
        Assertions.assertEquals(testItem1.getId(), resultDto.getId());
        Assertions.assertEquals(testItem1.getName(), resultDto.getName());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(testItem1.getId());
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findByItemId(testItem1.getId());
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findLastBookingDatetime(testItem1.getId());
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findNextBookingDatetime(testItem1.getId());
    }

    @Test
    public void testAddCommentToItemIncorrect() {

        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        CommentsDTO inputCommentDto = new CommentsDTO();
        inputCommentDto.setText("Отличная вещь, спасибо!");

        Mockito.when(userRepositoryMock.findById(testUser1.getId()))
                .thenReturn(Optional.of(testUser1));

        testItem1.setAvailable(true);
        Mockito.when(itemRepositoryMock.findById(testItem1.getId()))
                .thenReturn(Optional.of(testItem1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemService.addCommentToItem(testItem1.getId(), inputCommentDto, testUser1.getId())
        );

        Assertions.assertEquals(
                "Отзыв может оставить только тот пользователь, который брал эту вещь в аренду, и только после окончания срока аренды!",
                exception.getMessage()
        );

        Mockito.verify(commentRepositoryMock, Mockito.never()).save(Mockito.any(Comment.class));
    }

    @Test
    public void testAddCommentToItemWhenItemNotAvailable() {
        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        CommentsDTO inputCommentDto = new CommentsDTO();
        inputCommentDto.setText("Тестовый комментарий");

        Mockito.when(userRepositoryMock.findById(testUser1.getId()))
                .thenReturn(Optional.of(testUser1));

        testItem1.setAvailable(false);
        Mockito.when(itemRepositoryMock.findById(testItem1.getId()))
                .thenReturn(Optional.of(testItem1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemService.addCommentToItem(testItem1.getId(), inputCommentDto, testUser1.getId())
        );

        Assertions.assertEquals(
                "Вещь не доступна для бронирования!",
                exception.getMessage()
        );

        Mockito.verify(bookingRepositoryMock, Mockito.never())
                .findAllByUserIdAndState(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
        Mockito.verify(commentRepositoryMock, Mockito.never())
                .save(Mockito.any(Comment.class));
    }

    @Test
    public void testInsertItemWhenOwnerIdIsNull() {

        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        ItemDTO inputDto = itemMapper.itemToItemDTO(testItem1);


        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemService.insertItem(inputDto, null)
        );


        Assertions.assertEquals(
                "При создании предмета не указан его владелец",
                exception.getMessage()
        );

        Mockito.verify(userRepositoryMock, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(itemRepositoryMock, Mockito.never()).save(Mockito.any(Item.class));
    }

    @Test
    public void testUpdateItemWhenUserIsNotOwner() {

        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        ItemDTO updateDto = ItemDTO.builder()
                .name("Новое название")
                .build();

        Long wrongOwnerId = 999L;
        User wrongUser = new User();
        wrongUser.setId(wrongOwnerId);
        wrongUser.setName("Чужой пользователь");

        Mockito.when(userRepositoryMock.findById(wrongOwnerId))
                .thenReturn(Optional.of(wrongUser));

        Mockito.when(itemRepositoryMock.findById(testItem1.getId()))
                .thenReturn(Optional.of(testItem1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemService.updateItem(testItem1.getId(), updateDto, wrongOwnerId)
        );

        Assertions.assertEquals(
                "Указанный пользователь не является владельцем его вещи. Редактировать вещь может только её владелец.",
                exception.getMessage()
        );

        Mockito.verify(itemRepositoryMock, Mockito.never()).save(Mockito.any(Item.class));
    }

    @Test
    public void testSearchItemBySearchTextWhenTextIsEmptyOrNull() {
        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        Collection<ItemDTO> resultWithEmpty = itemService.searchItemBySearchText("");
        Assertions.assertTrue(resultWithEmpty.isEmpty());

        Collection<ItemDTO> resultWithNull = itemService.searchItemBySearchText(null);
        Assertions.assertTrue(resultWithNull.isEmpty());

        Mockito.verify(itemRepositoryMock, Mockito.never()).findByNameDescription(Mockito.anyString());
    }

    @Test
    public void testSearchItemBySearchTextValid() {
        ItemService itemService = new ItemServiceImpl(
                itemMapper,
                itemRepositoryMock,
                userRepositoryMock,
                commentRepositoryMock,
                commentMapper,
                bookingRepositoryMock
        );

        String textToSearch = "дрель";

        Mockito.when(itemRepositoryMock.findByNameDescription(textToSearch))
                .thenReturn(List.of(testItem1, testItem2));

        Collection<ItemDTO> result = itemService.searchItemBySearchText(textToSearch);

        Assertions.assertEquals(2, result.size());

        List<ItemDTO> resultList = result.stream().toList();
        Assertions.assertEquals(testItem1.getId(), resultList.get(0).getId());
        Assertions.assertEquals(testItem1.getName(), resultList.get(0).getName());
        Assertions.assertEquals(testItem2.getId(), resultList.get(1).getId());
        Assertions.assertEquals(testItem2.getName(), resultList.get(1).getName());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findByNameDescription(textToSearch);
    }

}
