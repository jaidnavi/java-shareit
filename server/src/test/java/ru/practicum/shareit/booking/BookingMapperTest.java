package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.mapping.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class BookingMapperTest {

    // Используем @Spy, чтобы маппер мог вызывать реальные методы внутренних мапперов
    @Spy
    private ItemMapper itemMapper;
    @Spy
    private UserMapper userMapper;

    private BookingMapper bookingMapper;

    private User testUser;
    private Item testItem;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        bookingMapper = new BookingMapper(itemMapper, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Роман");
        testUser.setEmail("kosticin@test.ru");

        testItem = new Item();
        testItem.setId(10L);
        testItem.setName("Дрель");
        testItem.setDescription("Мощная дрель");
        testItem.setAvailable(true);

        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(2);
    }

    @Test
    public void testToBookingCorrect() {
        // Подготавливаем входящий запрос DTO
        BookingRequestDTO requestDTO = BookingRequestDTO.builder()
                .itemId(10L)
                .start(start)
                .end(end)
                .build();

        // Вызываем маппинг в сущность Booking
        Booking booking = bookingMapper.toBooking(requestDTO, testItem, testUser);

        // Проверяем корректность переноса полей
        Assertions.assertNotNull(booking);
        Assertions.assertEquals(testItem, booking.getItem());
        Assertions.assertEquals(testUser, booking.getUser());
        Assertions.assertEquals(start, booking.getBookingStart());
        Assertions.assertEquals(end, booking.getBookingEnd());
    }

    @Test
    public void testToBookingWithNullValues() {
        BookingRequestDTO requestDTO = BookingRequestDTO.builder().build(); // Все поля null

        // Проверяем защиту от NullPointerException при пустых полях
        Booking booking = bookingMapper.toBooking(requestDTO, null, null);

        Assertions.assertNotNull(booking);
        Assertions.assertNull(booking.getItem());
        Assertions.assertNull(booking.getUser());
        Assertions.assertNull(booking.getBookingStart());
        Assertions.assertNull(booking.getBookingEnd());
    }

    @Test
    public void testToBookingDTOCorrect() {
        // Создаем заполненный объект сущности Booking
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setItem(testItem);
        booking.setUser(testUser);
        booking.setBookingStart(start);
        booking.setBookingEnd(end);
        booking.setStatus("WAITING");

        // Вызываем маппинг в BookingResponseDTO
        BookingResponseDTO responseDTO = bookingMapper.toBookingDTO(booking);

        // Проверяем базовые поля бронирования
        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(100L, responseDTO.getId());
        Assertions.assertEquals(start, responseDTO.getStart());
        Assertions.assertEquals(end, responseDTO.getEnd());
        Assertions.assertEquals("WAITING", responseDTO.getStatus());
        Assertions.assertEquals(10L, responseDTO.getItemId());

        // Проверяем, что вложенные DTO были успешно смапплены через itemMapper и userMapper
        Assertions.assertNotNull(responseDTO.getItem());
        Assertions.assertEquals("Дрель", responseDTO.getItem().getName());

        Assertions.assertNotNull(responseDTO.getBooker());
        Assertions.assertEquals("Роман", responseDTO.getBooker().getName());
    }

    @Test
    public void testToBookingDTOWithNullEntities() {
        Booking booking = new Booking();
        booking.setId(200L);
        booking.setItem(null);
        booking.setUser(null);

        // Проверяем, что метод не падает, если к бронированию не привязана вещь или пользователь
        BookingResponseDTO responseDTO = bookingMapper.toBookingDTO(booking);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(200L, responseDTO.getId());
        Assertions.assertNull(responseDTO.getItem());
        Assertions.assertNull(responseDTO.getItemId());
        Assertions.assertNull(responseDTO.getBooker());
    }
}