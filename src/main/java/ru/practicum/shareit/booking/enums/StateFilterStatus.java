package ru.practicum.shareit.booking.enums;

/**
 * Статусы для фильтра используемого в getBookingsByBooker и getBookingsByOwner:
 *     ALL - все бронирования
 *     CURRENT - текущие бронирования
 *     PAST - завершённые бронирования
 *     FUTURE - будущие бронирования
 *     WAITING - ожидают подтверждения
 *     REJECTED - отклонённые бронирования
 */

public enum StateFilterStatus {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED
}
