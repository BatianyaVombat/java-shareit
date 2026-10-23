package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public Collection<BookingDto> getBookingsByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByBooker(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto bookingApprove(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        return bookingService.approvedBooking(userId, bookingId, approved);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByOwner(userId, state);
    }
}
