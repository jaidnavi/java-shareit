package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;

import ru.practicum.shareit.booking.enumeration.StateEnum;

import java.util.List;

/**
 * Интерфейс функционала функции бронирования
 */
public interface BookingService {

    /**
     * Метод добавления бронирования
     *
     * @param bookingDTO данные нового бронирования
     * @param renterId   пользователь, осуществлюящий бронирование
     * @return объект добавленного бронирования
     */
    BookingResponseDTO addBooking(BookingRequestDTO bookingDTO, Long renterId);

    /**
     * Метод подтверждения или отклонения запроса на бронирование.
     *
     * @param bookingId   идентификатор бронирования
     * @param itemOwnerId пользователь, осуществлюящий подтверждение или отклонение
     * @param approved    действие по запросу. true - подверждение бронирования. false - отказ бронирования.
     * @return объект бронирования, после подтверждения или отклонения
     */
    BookingResponseDTO confirmOrReject(Long bookingId, Long itemOwnerId, Boolean approved);

    /**
     * Метод получения данных о конкретном бронировании (включая его статус)
     *
     * @param bookingId идентификатор бронирования
     * @param userId    пользователь, запрашивающий информацию
     * @return объект бронирования
     */
    BookingResponseDTO getBookingById(Long bookingId, Long userId);

    /**
     * Получение списка всех бронирований текущего пользователя.
     *
     * @param currentUserId идентификатор пользователя
     * @param state         параметр поиска.
     *                      Параметр state необязательный и по умолчанию равен ALL (англ. «все»).
     *                      Также он может принимать значения CURRENT (англ. «текущие»),
     *                      PAST (англ. «завершённые»),
     *                      FUTURE (англ. «будущие»),
     *                      WAITING (англ. «ожидающие подтверждения»),
     *                      REJECTED (англ. «отклонённые»).
     * @return список объектов бронирования, отсортированные по дате от более новых к более старым
     */
    List<BookingResponseDTO> getAllBookingByUserId(Long currentUserId, StateEnum state);


    /**
     * Получение списка бронирований для всех вещей текущего пользователя.
     *
     * @param currentUserId идентификатор пользователя
     * @param state         параметр поиска.
     *                      Параметр state необязательный и по умолчанию равен ALL (англ. «все»).
     *                      Также он может принимать значения CURRENT (англ. «текущие»),
     *                      PAST (англ. «завершённые»),
     *                      FUTURE (англ. «будущие»),
     *                      WAITING (англ. «ожидающие подтверждения»),
     *                      REJECTED (англ. «отклонённые»).
     * @return список объектов бронирования, отсортированные по дате от более новых к более старым
     */
    List<BookingResponseDTO> getAllBookingByOwnerId(Long currentUserId, StateEnum state);

}
