package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * TODO Sprint add-item-requests.
 */

@Getter
@Setter
@Builder
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса вещи не может быть пустым")
    private String description;

    private LocalDateTime created;

    @JsonProperty("items")
    private Collection<ItemRequestAnswerDto> answers;
}
