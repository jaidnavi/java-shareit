package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDTO;
import ru.practicum.shareit.request.mapping.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepositoryMock;
    @Mock
    private UserRepository userRepositoryMock;
    @Mock
    private ItemRepository itemRepositoryMock;

    private ItemRequestMapper itemRequestMapper;

    private ItemRequestService itemRequestService;

    private User testUser;
    private ItemRequest testRequest;
    private ItemRequestDTO requestDTO;
    private Item testItem;

    @BeforeEach
    void settingBeforeEach() {
        itemRequestMapper = new ItemRequestMapper();

        itemRequestService = new ItemRequestServiceImpl(
                itemRequestRepositoryMock,
                userRepositoryMock,
                itemRepositoryMock,
                itemRequestMapper
        );

        itemRequestService = new ItemRequestServiceImpl(
                itemRequestRepositoryMock,
                userRepositoryMock,
                itemRepositoryMock,
                itemRequestMapper
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Роман");

        testRequest = new ItemRequest();
        testRequest.setId(10L);
        testRequest.setDescription("Нужна дрель");
        testRequest.setUser(testUser);
        testRequest.setCreationDate(LocalDateTime.now());

        requestDTO = ItemRequestDTO.builder()
                .id(10L)
                .description("Нужна дрель")
                .build();

        testItem = new Item();
        testItem.setId(100L);
        testItem.setName("Дрель");
        testItem.setRequestId(10L);
        testItem.setUser(testUser);
    }

    @Test
    void testAddNewRequestCorrect() {
        Mockito.when(userRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(itemRequestRepositoryMock.save(any(ItemRequest.class))).thenReturn(testRequest);

        ItemRequestDTO result = itemRequestService.addNewRequest(requestDTO, testUser.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(10L, result.getId());
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).save(any(ItemRequest.class));
    }

    @Test
    void testAddNewRequestWhenBodyIsNullThrowsException() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemRequestService.addNewRequest(null, testUser.getId())
        );
        Assertions.assertEquals("Не заполнено тело запроса!", exception.getMessage());
    }

    @Test
    void testAddNewRequestWhenUserNotFoundThrowsException() {
        Mockito.when(userRepositoryMock.findById(999L)).thenReturn(Optional.empty());

        NoDataFoundException exception = assertThrows(NoDataFoundException.class, () ->
                itemRequestService.addNewRequest(requestDTO, 999L)
        );
        Assertions.assertTrue(exception.getMessage().contains("Пользователь с id 999 не найден"));
    }

    @Test
    void testGetItemRequestsWhenAllIsFalse() {
        Mockito.when(userRepositoryMock.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Должен вызваться метод findByUserIdOrderByCreationDateDesc
        Mockito.when(itemRequestRepositoryMock.findByUserIdOrderByCreationDateDesc(testUser.getId()))
                .thenReturn(List.of(testRequest));
        Mockito.when(itemRepositoryMock.findAllByRequestIdIn(anyList())).thenReturn(List.of(testItem));

        List<ItemRequestDTO> result = itemRequestService.getItemRequests(testUser.getId(), false);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Нужна дрель", result.get(0).getDescription());
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1))
                .findByUserIdOrderByCreationDateDesc(testUser.getId());
    }

    @Test
    void testGetItemRequestsWhenAllIsTrue() {
        Mockito.when(userRepositoryMock.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Должен вызваться метод findByUserIdNotOrderByCreationDateDesc
        Mockito.when(itemRequestRepositoryMock.findByUserIdNotOrderByCreationDateDesc(testUser.getId()))
                .thenReturn(List.of(testRequest));
        Mockito.when(itemRepositoryMock.findAllByRequestIdIn(anyList())).thenReturn(List.of(testItem));

        List<ItemRequestDTO> result = itemRequestService.getItemRequests(testUser.getId(), true);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Нужна дрель", result.get(0).getDescription());
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1))
                .findByUserIdNotOrderByCreationDateDesc(testUser.getId());
    }

    @Test
    void testGetItemRequestByIdCorrect() {
        Mockito.when(itemRequestRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(testRequest));
        Mockito.when(itemRepositoryMock.findAllByRequestIdIn(anyList())).thenReturn(List.of(testItem));

        ItemRequestDTO result = itemRequestService.getItemRequestById(testRequest.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(10L, result.getId());
        Assertions.assertEquals("Нужна дрель", result.getDescription());
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findById(testRequest.getId());
    }

    @Test
    void testGetItemRequestByIdNotFoundThrowsException() {
        Mockito.when(itemRequestRepositoryMock.findById(999L)).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemRequestService.getItemRequestById(999L)
        );
        Assertions.assertEquals("Запрос с id 999не найден", exception.getMessage());
    }
}