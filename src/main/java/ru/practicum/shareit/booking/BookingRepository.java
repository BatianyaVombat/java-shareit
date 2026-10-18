package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {
    private final Map<Long, Booking> bookingTable = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Optional<Booking> getBookingById(Long bookingId) {
        if (bookingId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(bookingTable.get(bookingId));
    }

    public Collection<Booking> findAllByOwnerId(Long ownerId) {
        return bookingTable.values().stream()
                .filter(b -> b.getItem().getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public Collection<Booking> findAllByBookerId(Long bookerId) {
        return bookingTable.values().stream()
                .filter(b -> b.getBooker().getId().equals(bookerId))
                .collect(Collectors.toList());
    }

    public Optional<Booking> findLastByItemId(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingTable.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd));
    }

    public Optional<Booking> findNextByItemId(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingTable.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getEnd().isAfter(now))
                .max(Comparator.comparing(Booking::getStart));
    }

    public Booking createBooking(Booking booking) {

        Booking newBooking = Booking.builder()
                .id(idCounter.getAndIncrement())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();

        bookingTable.put(newBooking.getId(), newBooking);
        return newBooking;
    }

    public void approvedBooking(Booking booking) {
        bookingTable.put(booking.getId(), booking);
    }

    //подтверждение существования подтверждённого и завершённого бронирования
    public Boolean existsByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, LocalDateTime now) {
        return bookingTable.values().stream()
                .anyMatch(b -> b.getItem().getId().equals(itemId)
                        && b.getBooker().getId().equals(bookerId)
                        && b.getEnd().isBefore(now)
                        && b.getStatus().equals(BookingStatus.APPROVED));

    }
}
