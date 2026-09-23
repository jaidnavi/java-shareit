package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.enumeration.StateEnum;
import ru.practicum.shareit.booking.mapping.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


@ActiveProfiles("test")
@Transactional
@SpringBootTest()
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepositoryMock;
    @Mock
    private UserRepository userRepositoryMock;
    @Mock
    private ItemRepository itemRepositoryMock;
    @Mock
    private BookingMapper bookingMapperMock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingRequestDTO requestDTO;
    private BookingResponseDTO responseDTO;

    @BeforeEach
    void settingBeforeEach() {
        bookingService = new BookingServiceImpl(
                bookingRepositoryMock,
                userRepositoryMock,
                itemRepositoryMock,
                bookingMapperMock
        );

        booker = new User();
        booker.setId(1L);
        booker.setName("Роман");

        owner = new User();
        owner.setId(2L);
        owner.setName("Владелец");

        item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setAvailable(true);
        item.setUser(owner);

        booking = new Booking();
        booking.setId(100L);
        booking.setUser(booker);
        booking.setItem(item);
        booking.setStatus("WAITING");

        requestDTO = BookingRequestDTO.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        responseDTO = BookingResponseDTO.builder()
                .id(100L)
                .status("WAITING")
                .build();
    }


    @Test
    void testAddBookingCorrect() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingMapperMock.toBooking(any(), any(), any())).thenReturn(booking);
        Mockito.when(bookingRepositoryMock.save(any(Booking.class))).thenReturn(booking);
        Mockito.when(bookingMapperMock.toBookingDTO(any())).thenReturn(responseDTO);

        BookingResponseDTO result = bookingService.addBooking(requestDTO, booker.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(100L, result.getId());
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).save(any(Booking.class));
    }

    @Test
    void testAddBookingWhenRentingOwnItemThrowsException() {
        Mockito.when(userRepositoryMock.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepositoryMock.findById(item.getId())).thenReturn(Optional.of(item));

        NoDataFoundException ex = assertThrows(NoDataFoundException.class, () ->
                bookingService.addBooking(requestDTO, owner.getId())
        );
        Assertions.assertEquals("Нельзя бронировать собственную вещь", ex.getMessage());
    }

    @Test
    void testConfirmOrRejectApprove() {
        Mockito.when(bookingRepositoryMock.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepositoryMock.save(any(Booking.class))).thenReturn(booking);

        responseDTO.setStatus("APPROVED");
        Mockito.when(bookingMapperMock.toBookingDTO(any())).thenReturn(responseDTO);

        BookingResponseDTO result = bookingService.confirmOrReject(booking.getId(), owner.getId(), true);

        Assertions.assertEquals("APPROVED", result.getStatus());
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).save(booking);
    }

    @Test
    void testConfirmOrRejectNotByOwnerThrowsException() {
        Mockito.when(bookingRepositoryMock.findById(booking.getId())).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                bookingService.confirmOrReject(booking.getId(), booker.getId(), true)
        );
        Assertions.assertTrue(ex.getMessage().contains("может быть выполнено только владельцем вещи"));
    }


    @Test
    void testGetBookingByIdForBookerOrOwner() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        BookingResponseDTO result = bookingService.getBookingById(booking.getId(), booker.getId());

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(booking.getId());
    }


    @Test
    void testGetAllBookingByUserIdStateAll() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findAllByUserIdOrderByBookingStartDesc(booker.getId()))
                .thenReturn(List.of(booking));

        List<BookingResponseDTO> result = bookingService.getAllBookingByUserId(booker.getId(), StateEnum.ALL);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByUserIdOrderByBookingStartDesc(booker.getId());
    }

    @Test
    void testGetAllBookingByOwnerIdStateAll() {

        Mockito.when(userRepositoryMock.findById(owner.getId()))
                .thenReturn(Optional.of(owner));


        Mockito.when(bookingRepositoryMock.findAllByItemUserIdOrderByBookingStartDesc(owner.getId()))
                .thenReturn(List.of(booking));


        Mockito.when(bookingMapperMock.toBookingDTO(booking))
                .thenReturn(responseDTO);


        List<BookingResponseDTO> result = bookingService.getAllBookingByOwnerId(owner.getId(), StateEnum.ALL);


        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(responseDTO.getId(), result.get(0).getId());

        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByItemUserIdOrderByBookingStartDesc(owner.getId());
    }

    @Test
    void testGetAllBookingByOwnerIdStateFuture() {
        Mockito.when(userRepositoryMock.findById(owner.getId()))
                .thenReturn(Optional.of(owner));


        Mockito.when(bookingRepositoryMock.findAllByOwnerIdAndState(
                        eq(owner.getId()),
                        eq("APPROVED"),
                        eq("FUTURE"),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        Mockito.when(bookingMapperMock.toBookingDTO(booking))
                .thenReturn(responseDTO);


        List<BookingResponseDTO> result = bookingService.getAllBookingByOwnerId(owner.getId(), StateEnum.FUTURE);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByOwnerIdAndState(eq(owner.getId()), eq("APPROVED"), eq("FUTURE"), any(LocalDateTime.class));
    }


    @Test
    void testGetAllBookingByOwnerIdStatePast() {
        Mockito.when(userRepositoryMock.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findAllByOwnerIdAndState(
                        eq(owner.getId()), eq("APPROVED"), eq("PAST"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByOwnerId(owner.getId(), StateEnum.PAST);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByOwnerIdAndState(eq(owner.getId()), eq("APPROVED"), eq("PAST"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByOwnerIdStateCurrent() {
        Mockito.when(userRepositoryMock.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findAllByOwnerIdAndState(
                        eq(owner.getId()), eq("APPROVED"), eq("CURRENT"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByOwnerId(owner.getId(), StateEnum.CURRENT);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByOwnerIdAndState(eq(owner.getId()), eq("APPROVED"), eq("CURRENT"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByOwnerIdStateWaiting() {
        Mockito.when(userRepositoryMock.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findAllByOwnerIdAndState(
                        eq(owner.getId()), eq("WAITING"), eq("WAITING"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByOwnerId(owner.getId(), StateEnum.WAITING);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByOwnerIdAndState(eq(owner.getId()), eq("WAITING"), eq("WAITING"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByOwnerIdStateRejected() {
        Mockito.when(userRepositoryMock.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findAllByOwnerIdAndState(
                        eq(owner.getId()), eq("REJECTED"), eq("REJECTED"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByOwnerId(owner.getId(), StateEnum.REJECTED);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByOwnerIdAndState(eq(owner.getId()), eq("REJECTED"), eq("REJECTED"), any(LocalDateTime.class));
    }

    @Test
    void testValidateStateAndUserWhenStateIsNull() {

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.getAllBookingByUserId(1L, null)
        );
        Assertions.assertEquals("Параметр статуса бронирования должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testValidateStateAndUserWhenUserIdIsNull() {

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.getAllBookingByUserId(null, StateEnum.ALL)
        );
        Assertions.assertEquals("Идентификатор пользователя должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testChekItemWhenItemIdIsNull() {

        BookingRequestDTO badRequest = BookingRequestDTO.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.addBooking(badRequest, booker.getId())
        );
        Assertions.assertEquals("Идентификатор вещи должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testChekItemWhenItemNotAvailable() {

        item.setAvailable(false);

        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(item.getId())).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.addBooking(requestDTO, booker.getId())
        );
        Assertions.assertEquals("Вещь не доступна для бронирования!", exception.getMessage());
    }

    @Test
    void testChekNewBookingWhenDatesAreIncorrect() {
        // Устанавливаем дату окончания раньше даты начала
        BookingRequestDTO badRequest = BookingRequestDTO.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(item.getId())).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.addBooking(badRequest, booker.getId())
        );
        Assertions.assertEquals("Некорректные даты бронирования", exception.getMessage());
    }

    @Test
    void testChekParamIsNotNullWhenBookingIdIsNull() {

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.confirmOrReject(null, owner.getId(), true)
        );
        Assertions.assertEquals("Идентификатор бронирования должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testChekParamIsNotNullWhenItemOwnerIdIsNull() {

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.confirmOrReject(booking.getId(), null, true)
        );
        Assertions.assertEquals("Идентификатор владельца должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testChekParamIsNotNullWhenApprovedIsNull() {

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.confirmOrReject(booking.getId(), owner.getId(), null)
        );
        Assertions.assertEquals("Параметр approved должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testGetBookingByIdWhenUserIsNeitherBookerNorOwnerThrowsException() {
        Long strangerUserId = 999L;

        Mockito.when(userRepositoryMock.findById(strangerUserId))
                .thenReturn(Optional.of(new User()));

        Mockito.when(bookingRepositoryMock.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        NoDataFoundException exception = assertThrows(NoDataFoundException.class, () ->
                bookingService.getBookingById(booking.getId(), strangerUserId)
        );

        Assertions.assertEquals(
                "Запрос может быть выполнен либо автором бронирования, либо владельцем вещи",
                exception.getMessage()
        );

        Mockito.verify(bookingMapperMock, Mockito.never()).toBookingDTO(any());
    }

    @Test
    void testConfirmOrRejectWhenBookingAlreadyProcessedThrowsException() {
        booking.setStatus("APPROVED");

        Mockito.when(bookingRepositoryMock.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.confirmOrReject(booking.getId(), owner.getId(), true)
        );
        Assertions.assertEquals("Бронирование уже обработано", exception.getMessage());
    }

    @Test
    void testCheckBookingWhenBookingIdIsNullThrowsException() {
        Mockito.when(userRepositoryMock.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.getBookingById(null, booker.getId())
        );
        Assertions.assertEquals("Идентификатор бронирования должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testChekUserWhenUserIdIsNullThrowsException() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bookingService.getBookingById(booking.getId(), null)
        );
        Assertions.assertEquals("Идентификатор пользователя должен быть заполнен!", exception.getMessage());
    }

    @Test
    void testGetAllBookingByUserIdStatePast() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findAllByUserIdAndState(
                        eq(booker.getId()), eq("APPROVED"), eq("PAST"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByUserId(booker.getId(), StateEnum.PAST);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByUserIdAndState(eq(booker.getId()), eq("APPROVED"), eq("PAST"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByUserIdStateCurrent() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findAllByUserIdAndState(
                        eq(booker.getId()), eq("APPROVED"), eq("CURRENT"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByUserId(booker.getId(), StateEnum.CURRENT);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByUserIdAndState(eq(booker.getId()), eq("APPROVED"), eq("CURRENT"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByUserIdStateFuture() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findAllByUserIdAndState(
                        eq(booker.getId()), eq("APPROVED"), eq("FUTURE"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByUserId(booker.getId(), StateEnum.FUTURE);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByUserIdAndState(eq(booker.getId()), eq("APPROVED"), eq("FUTURE"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByUserIdStateWaiting() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findAllByUserIdAndState(
                        eq(booker.getId()), eq("WAITING"), eq("WAITING"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByUserId(booker.getId(), StateEnum.WAITING);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByUserIdAndState(eq(booker.getId()), eq("WAITING"), eq("WAITING"), any(LocalDateTime.class));
    }

    @Test
    void testGetAllBookingByUserIdStateRejected() {
        Mockito.when(userRepositoryMock.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findAllByUserIdAndState(
                        eq(booker.getId()), eq("REJECTED"), eq("REJECTED"), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapperMock.toBookingDTO(booking)).thenReturn(responseDTO);

        List<BookingResponseDTO> result = bookingService.getAllBookingByUserId(booker.getId(), StateEnum.REJECTED);

        Assertions.assertNotNull(result);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1))
                .findAllByUserIdAndState(eq(booker.getId()), eq("REJECTED"), eq("REJECTED"), any(LocalDateTime.class));
    }


}