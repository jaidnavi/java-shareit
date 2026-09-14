package ru.practicum.shareit.booking.mapping;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;

@Component
public class BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public BookingMapper(ItemMapper itemMapper, UserMapper userMapper) {
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }


    public Booking toBooking(BookingRequestDTO bookingDTO, Item item, User user) {
        Booking booking = new Booking();
        if (item != null) {
            booking.setItem(item);
        }
        if (user != null) {
            booking.setUser(user);
        }
        if (bookingDTO.getStart() != null) {
            booking.setBookingStart(bookingDTO.getStart());
        }
        if (bookingDTO.getEnd() != null) {
            booking.setBookingEnd(bookingDTO.getEnd());
        }
        return booking;
    }

    public BookingResponseDTO toBookingDTO(Booking booking) {

        BookingResponseDTO bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setId(booking.getId());
        if (booking.getItem() != null) {
            bookingResponseDTO.setItem(itemMapper.itemToItemDTO(booking.getItem()));
            bookingResponseDTO.setItemId(booking.getItem().getId());
        }

        if (booking.getUser() != null) {
            bookingResponseDTO.setBooker(userMapper.userToUserDTO(booking.getUser()));
        }

        bookingResponseDTO.setStart(booking.getBookingStart());
        bookingResponseDTO.setEnd(booking.getBookingEnd());
        bookingResponseDTO.setStatus(booking.getStatus());

        return bookingResponseDTO;
    }
}
