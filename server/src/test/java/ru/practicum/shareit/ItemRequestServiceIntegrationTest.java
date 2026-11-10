package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRep;

    @Autowired
    private ItemService itemService;

    private User requester;
    private User responder;

    @BeforeEach
    void setUp() {
        requester = userRep.save(User.builder().name("Просящий").email("req@mail.com").build());
        responder = userRep.save(User.builder().name("Отвечающий").email("resp@mail.com").build());
    }

    @Test
    void shouldCreateRequest() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Ищу дрель").build();

        ItemRequestDto created = itemRequestService.createRequest(requester.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Ищу дрель");
        assertThat(created.getAnswers()).isEmpty();
    }

    @Test
    void shouldThrowWhenUserNotFoundOnCreate() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Ищу дрель").build();

        assertThatThrownBy(() -> itemRequestService.createRequest(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetOwnRequests() {
        itemRequestService.createRequest(requester.getId(), ItemRequestDto.builder().description("Запрос 1").build());
        itemRequestService.createRequest(requester.getId(), ItemRequestDto.builder().description("Запрос 2").build());

        Collection<ItemRequestDto> result = itemRequestService.getOwnRequests(requester.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldGetOtherRequests() {
        itemRequestService.createRequest(requester.getId(), ItemRequestDto.builder().description("Мой запрос").build());

        Collection<ItemRequestDto> result = itemRequestService.getOtherRequests(responder.getId());

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getDescription()).isEqualTo("Мой запрос");
    }

    @Test
    void shouldGetRequestByIdWithAnswers() {
        ItemRequestDto request = itemRequestService.createRequest(
                requester.getId(), ItemRequestDto.builder().description("Ищу дрель").build());

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Хорошая")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(responder.getId(), itemDto);

        ItemRequestDto found = itemRequestService.getRequestById(request.getId());

        assertThat(found.getAnswers()).hasSize(1);
        assertThat(found.getAnswers().iterator().next().getName()).isEqualTo("Дрель");
        assertThat(found.getAnswers().iterator().next().getOwnerId()).isEqualTo(responder.getId());
    }

    @Test
    void shouldThrowWhenRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
