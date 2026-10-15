package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRep;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = ItemMapper.toEntity(itemDto);
        item.setOwnerId(userId);

        Item created = itemRep.createItem(item);
        return ItemMapper.toResponse(created);
    }

    @Override
    public ItemDto updateItem(Long id, Long userId, UpdateItemRequest request) {
        Item oldItem = itemRep.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена"));

        if (!oldItem.getOwnerId().equals(userId)) {
            throw new NotFoundException("Вещь с id = " + id + " не найдена");
        }

        if (request.getName() != null) {
            oldItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            oldItem.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            oldItem.setAvailable(request.getAvailable());
        }

        Item updated = itemRep.updateItem(oldItem);
        return ItemMapper.toResponse(updated);
    }

    @Override
    public Collection<ItemDto> getAllUserItems(Long userId) {
        return itemRep.getAll(userId).stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRep.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
        return ItemMapper.toResponse(item);
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        return itemRep.searchItems(text).stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}
