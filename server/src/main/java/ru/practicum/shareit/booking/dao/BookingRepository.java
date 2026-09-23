package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByUserIdOrderByBookingStartDesc(Long userId);

    List<Booking> findAllByItemUserIdOrderByBookingStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "JOIN i.user u " +
            "WHERE u.id = :ownerId " +
            "  AND b.status = :status " +
            "  AND ( " +
            "       (:state = 'PAST'    AND b.bookingEnd   < :now) OR " +
            "       (:state = 'CURRENT' AND :now BETWEEN b.bookingStart AND b.bookingEnd) OR " +
            "       (:state = 'FUTURE'  AND b.bookingStart > :now) OR " +
            "       (:state = 'WAITING') OR " +
            "       (:state = 'REJECTED') " +
            "  ) " +
            "ORDER BY b.bookingStart DESC")
    List<Booking> findAllByOwnerIdAndState(@Param("ownerId") Long ownerId,
                                           @Param("status") String status,
                                           @Param("state") String state,
                                           @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.user u " +
            "WHERE u.id = :userId " +
            "  AND b.status = :status " +
            "  AND ( " +
            "       (:state = 'PAST'    AND b.bookingEnd   < :now) OR " +
            "       (:state = 'CURRENT' AND :now BETWEEN b.bookingStart AND b.bookingEnd) OR " +
            "       (:state = 'FUTURE'  AND b.bookingStart > :now) OR " +
            "       (:state = 'WAITING') OR " +
            "       (:state = 'REJECTED') " +
            "  ) " +
            "ORDER BY b.bookingStart DESC")
    List<Booking> findAllByUserIdAndState(@Param("userId") Long userId,
                                          @Param("status") String status,
                                          @Param("state") String state,
                                          @Param("now") LocalDateTime now);

    @Query("SELECT MAX(b.bookingEnd) FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "  AND b.status = 'APPROVED' " +
            "  AND b.bookingEnd < CURRENT_TIMESTAMP")
    LocalDateTime findLastBookingDatetime(@Param("itemId") Long itemId);

    @Query("SELECT MIN(b.bookingStart) FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "  AND b.status = 'APPROVED' " +
            "  AND b.bookingStart > CURRENT_TIMESTAMP")
    LocalDateTime findNextBookingDatetime(@Param("itemId") Long itemId);
}