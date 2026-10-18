package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRep;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

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
        Collection<Item> items = itemRep.getAll(userId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsByItemId = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toResponse(item);
                    dto.setComments(commentsByItemId.getOrDefault(item.getId(), List.of()).stream()
                            .map(CommentMapper::toResponse)
                            .collect(Collectors.toList()));

                    Booking last = bookingRepository.findLastByItemId(item.getId()).orElse(null);
                    Booking next = bookingRepository.findNextByItemId(item.getId()).orElse(null);
                    dto.setLastBooking(last != null ? BookingMapper.toShortDto(last) : null);
                    dto.setNextBooking(next != null ? BookingMapper.toShortDto(next) : null);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRep.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
        Collection<Comment> itemComments = commentRepository.findAllByItemId(itemId);

        ItemDto dtoItem = ItemMapper.toResponse(item);
        Collection<CommentDto> dtoComment = itemComments.stream().map(CommentMapper::toResponse).toList();
        dtoItem.setComments(itemComments.stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList()));

        boolean isOwner = item.getOwnerId().equals(userId);
        if (isOwner) {
            Booking last = bookingRepository.findLastByItemId(itemId).orElse(null);
            Booking next = bookingRepository.findNextByItemId(itemId).orElse(null);
            dtoItem.setLastBooking(last != null ? BookingMapper.toShortDto(last) : null);
            dtoItem.setNextBooking(next != null ? BookingMapper.toShortDto(next) : null);
        }

        return dtoItem;
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        return itemRep.searchItems(text).stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto dto) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRep.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));

        if (!bookingRepository.existsByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now())) {
            throw new ValidationException("Пользователь не брал вещь в аренду");
        }

        Comment comment = CommentMapper.toEntity(dto, item, user);
        Comment created = commentRepository.createComment(comment);
        return CommentMapper.toResponse(created);
    }

    @Override
    public Collection<CommentDto> getCommentsByItemId(Long itemId) {
        itemRep.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));

        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
