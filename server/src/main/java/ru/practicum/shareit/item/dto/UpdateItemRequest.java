package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateItemRequest {

    private String name;

    private String description;

    private Boolean available;
}
