package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.end < :now ORDER BY b.end DESC")
    Optional<Booking> findLastByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :now ORDER BY b.start ASC")
    Optional<Booking> findNextByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    boolean existsByItemIdAndBookerIdAndEndBeforeAndStatus(Long itemId, Long bookerId, LocalDateTime now, BookingStatus status);

    List<Booking> findAllByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId);
}
