package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemRequestAnswerDto {
    private Long id;

    private String name;

    private Long ownerId;
}
