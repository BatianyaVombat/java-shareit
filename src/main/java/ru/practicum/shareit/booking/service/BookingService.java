package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;


public interface BookingService {
    BookingDto createBooking(Long userId, BookingDto bookingDto);

    BookingDto approvedBooking(Long userId, Long bookingId, Boolean approved);

    Collection<BookingDto> getBookingsByBooker(Long userId, String state);

    Collection<BookingDto> getBookingsByOwner(Long userId, String state);

    BookingDto getBookingById(Long bookingId, Long userId);
}
