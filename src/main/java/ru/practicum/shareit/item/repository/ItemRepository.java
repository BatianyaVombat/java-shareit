package ru.practicum.shareit.item.repository;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Item createItem(Item item) {
        Item newItem = Item.builder()
                .id(idCounter.getAndIncrement())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .ownerId(item.getOwnerId())
                .build();

        itemStorage.put(newItem.getId(), newItem);

        return newItem;
    }

    public Item updateItem(Item item) {
        if (item.getId() == null) {
            throw new ValidationException("Id вещи обязателен");
        }
        if (!itemStorage.containsKey(item.getId())) {
            throw new NotFoundException("Вещь с id = " + item.getId() + " не найдена");
        }
        itemStorage.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long itemId) {
        if (itemId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(itemStorage.get(itemId));
    }

    public Collection<Item> getAll(Long userId) {
        return itemStorage.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    public Collection<Item> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        return itemStorage.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }
}
