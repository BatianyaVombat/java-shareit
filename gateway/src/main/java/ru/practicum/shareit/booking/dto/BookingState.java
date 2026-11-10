package ru.practicum.shareit.booking.dto;

import java.util.Optional;

/**
 * Статусы бронирования:
 * WAITING - новое бронирование, ожидает одобрения
 * APPROVED - бронирование подтверждено владельцем
 * REJECTED - бронирование отклонено владельцем
 * CANCELED - бронирование отменено создателем
 */

public enum BookingState {
    ALL,
    CURRENT,
    FUTURE,
    PAST,
    REJECTED,
    WAITING;

    public static Optional<BookingState> from(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
