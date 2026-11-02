package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@Builder
public class ItemDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingShortDto lastBooking;

    private BookingShortDto nextBooking;

    private Collection<CommentDto> comments;

    private Long requestId;
}
