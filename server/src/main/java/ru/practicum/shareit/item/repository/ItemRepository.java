package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByNameContainingIgnoreCaseAndAvailableTrue(String text);

    List<Item> findAllByOwnerId(Long userId);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findAllByRequestIdIn(Collection<Long> ids);
}