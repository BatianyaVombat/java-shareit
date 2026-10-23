package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRep;
    private final UserRepository userRep;
    private final BookingRepository bookingRep;
    private final CommentRepository commentRep;

    public ItemDto createItem(Long userId, ItemDto dto) {
        User owner = userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Item newItem = ItemMapper.toEntity(dto);

        newItem.setOwner(owner);

        return ItemMapper.toResponse(itemRep.save(newItem));
    }

    public ItemDto updateItem(Long id, Long userId, UpdateItemRequest request) {
        Item oldItem = itemRep.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена"));

        if (oldItem.getOwner() == null || !oldItem.getOwner().getId().equals(userId)) {
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

        return ItemMapper.toResponse(itemRep.save(oldItem));
    }

    public Collection<ItemDto> findAllByOwnerId(Long userId) {
        Collection<Item> items = itemRep.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsByItemId = commentRep.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toResponse(item);
                    dto.setComments(commentsByItemId.getOrDefault(item.getId(), List.of()).stream()
                            .map(CommentMapper::toResponse)
                            .collect(Collectors.toList()));

                    Booking last = bookingRep.findLastByItemId(item.getId(), LocalDateTime.now()).orElse(null);
                    Booking next = bookingRep.findNextByItemId(item.getId(), LocalDateTime.now()).orElse(null);
                    dto.setLastBooking(last != null ? BookingMapper.toShortDto(last) : null);
                    dto.setNextBooking(next != null ? BookingMapper.toShortDto(next) : null);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRep.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
        Collection<Comment> itemComments = commentRep.findAllByItemId(itemId);

        ItemDto dtoItem = ItemMapper.toResponse(item);
        dtoItem.setComments(itemComments.stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList()));

        boolean isOwner = item.getOwner().getId().equals(userId);
        if (isOwner) {
            Booking last = bookingRep.findLastByItemId(itemId, LocalDateTime.now()).orElse(null);
            Booking next = bookingRep.findNextByItemId(itemId, LocalDateTime.now()).orElse(null);
            dtoItem.setLastBooking(last != null ? BookingMapper.toShortDto(last) : null);
            dtoItem.setNextBooking(next != null ? BookingMapper.toShortDto(next) : null);
        }

        return dtoItem;
    }

    public Collection<ItemDto> findByNameContainingIgnoreCaseAndAvailableTrue(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        return itemRep.findByNameContainingIgnoreCaseAndAvailableTrue(text).stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto createComment(Long userId, Long itemId, NewCommentRequest request) {
        User user = userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRep.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));

        if (!bookingRep.existsByItemIdAndBookerIdAndEndBeforeAndStatus(itemId, userId,
                LocalDateTime.now(), BookingStatus.APPROVED)) {
            throw new ValidationException("Пользователь не брал вещь в аренду");
        }

        Comment newComment = CommentMapper.toEntity(request, item, user);
        return CommentMapper.toResponse(commentRep.save(newComment));
    }
}
