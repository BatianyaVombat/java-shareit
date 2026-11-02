package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserShortDto;

@UtilityClass
public class BookingMapper {
    public BookingDto toResponse(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new UserShortDto(booking.getBooker().getId()))
                .item(new ItemShortDto(booking.getItem().getId(), booking.getItem().getName()))
                .status(booking.getStatus())
                .build();
    }

    public Booking toEntity(BookingDto bookingDto, User booker, Item item) {
        return Booking.builder()
                .booker(booker)
                .item(item)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    public static BookingShortDto toShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(new UserShortDto(booking.getBooker().getId()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}
