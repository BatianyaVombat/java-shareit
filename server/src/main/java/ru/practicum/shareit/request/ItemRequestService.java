package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRep;
    private final UserRepository userRep;
    private final ItemRepository itemRep;

    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        User requester = userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        ItemRequest newRequest = ItemRequestMapper.toEntity(dto, requester);
        ItemRequestDto response = ItemRequestMapper.toResponse(itemRequestRep.save(newRequest));
        response.setAnswers(List.of());

        return response;
    }

    @Transactional
    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest request = itemRequestRep.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));

        ItemRequestDto dto = ItemRequestMapper.toResponse(request);
        List<Item> answerItems = itemRep.findAllByRequestId(requestId);

        List<ItemRequestAnswerDto> answers = answerItems.stream()
                .map(ItemRequestMapper::toAnswerDto)
                .toList();

        dto.setAnswers(answers);

        return dto;
    }

    @Transactional
    public Collection<ItemRequestDto> getOwnRequests(Long userId) {
        userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        List<ItemRequest> requests = itemRequestRep.findAllByRequesterIdOrderByCreatedDescIdDesc(userId);
        return enrichWithAnswers(requests);
    }

    @Transactional
    public Collection<ItemRequestDto> getOtherRequests(Long userId) {
        userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        List<ItemRequest> requests = itemRequestRep.findAllByRequesterIdNotOrderByCreatedDescIdDesc(userId);
        return enrichWithAnswers(requests);
    }

    private Collection<ItemRequestDto> enrichWithAnswers(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> ids = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRep.findAllByRequestIdIn(ids);
        Map<Long, List<Item>> grouped = items.stream().collect(groupingBy(Item::getRequestId));

        return requests.stream()
                .map(r -> {
                    ItemRequestDto dto = ItemRequestMapper.toResponse(r);
                    dto.setAnswers(grouped.getOrDefault(r.getId(), List.of())
                            .stream()
                            .map(ItemRequestMapper::toAnswerDto)
                            .toList());
                    return dto;
                })
                .toList();
    }
}
