package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Setter
@Builder
public class BookingDto {
    private Long id;

    @NotNull(message = "Дата начала бронирования не может быть пустой")
    private LocalDateTime start;

    @NotNull(message = "Дата конца бронирования не может быть пустой")
    private LocalDateTime end;
    private BookingStatus status;
    private Long itemId;
    private ItemShortDto item;
    private UserShortDto booker;
}
