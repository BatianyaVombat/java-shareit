package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long id, Long userId, UpdateItemRequest request);

    Collection<ItemDto> getAllUserItems(Long ownerId);

    ItemDto getItemById(Long itemId, Long userId);

    Collection<ItemDto> searchItems(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto request);

}
