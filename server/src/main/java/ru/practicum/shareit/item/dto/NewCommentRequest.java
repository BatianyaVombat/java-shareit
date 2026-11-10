package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentRequest {

    private String text;
}
