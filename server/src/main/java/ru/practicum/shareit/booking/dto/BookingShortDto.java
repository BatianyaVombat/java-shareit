package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingShortDto {
    private Long id;
    private UserShortDto bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}
