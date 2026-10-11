package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateUserRequest {
    private Long id;

    private String name;

    @Email(message = "Электронная почта должна иметь корректный формат")
    private String email;
}
