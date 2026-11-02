package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRep;

    @Autowired
    private ItemRepository itemRep;

    @Autowired
    private BookingRepository bookingRep;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Владелец")
                .email("owner@example.com")
                .build();
        owner = userRep.save(owner);
    }

    @Test
    void shouldCreateItemAndFindItById() {
        ItemDto createDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(owner.getId(), createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Дрель");
        assertThat(created.getAvailable()).isTrue();

        //Проверяем, что вещь реально сохранилась в БД
        Item savedItem = itemRep.findById(created.getId()).orElseThrow();
        assertThat(savedItem.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void shouldThrowExceptionWhenCreatingItemForNonExistentUser() {
        ItemDto createDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.createItem(999L, createDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = 999 не найден");
    }

    @Test
    void shouldUpdateItem() {
        ItemDto createDto = ItemDto.builder()
                .name("Дрель")
                .description("Старая дрель")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(owner.getId(), createDto);

        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .name("Новая дрель")
                .description("Обновленная дрель")
                .available(false)
                .build();

        ItemDto updated = itemService.updateItem(created.getId(), owner.getId(), updateRequest);

        assertThat(updated.getName()).isEqualTo("Новая дрель");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void shouldGetItemById() {
        ItemDto createDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto created = itemService.createItem(owner.getId(), createDto);
        ItemDto found = itemService.getItemById(created.getId(), owner.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Дрель");
        assertThat(found.getDescription()).isEqualTo("Мощная дрель");
        assertThat(found.getAvailable()).isTrue();
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        assertThatThrownBy(() -> itemService.getItemById(999L, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с id = 999 не найдена");
    }

    @Test
    void shouldFindAllItemsByOwnerId() {
        // Создаем второго пользователя (не владельца)
        User anotherUser = User.builder()
                .name("Другой")
                .email("another@example.com")
                .build();
        anotherUser = userRep.save(anotherUser);

        //Создаем вещи для owner
        ItemDto item1 = ItemDto.builder()
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .build();

        ItemDto item2 = ItemDto.builder()
                .name("Молоток")
                .description("Тяжелый")
                .available(true)
                .build();
        itemService.createItem(owner.getId(), item1);
        itemService.createItem(owner.getId(), item2);

        //Создаем вещь для другого пользователя
        ItemDto foreignItem = ItemDto.builder()
                .name("Чужая вещь")
                .description("Не наша")
                .available(true)
                .build();
        itemService.createItem(anotherUser.getId(), foreignItem);

        Collection<ItemDto> result = itemService.findAllByOwnerId(owner.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void shouldFindByNameContainingIgnoreCaseAndAvailableTrue() {
        // Создаем три вещи с разными значением available
        ItemDto availableDrill = ItemDto.builder()
                .name("Электрическая Дрель")
                .description("Мощная")
                .available(true)
                .build();
        ItemDto unavailableDrill = ItemDto.builder()
                .name("Ручная дрель")
                .description("Старая")
                .available(false)
                .build();
        ItemDto hammer = ItemDto.builder()
                .name("Молоток")
                .description("Тяжелый")
                .available(true)
                .build();

        itemService.createItem(owner.getId(), availableDrill);
        itemService.createItem(owner.getId(), unavailableDrill);
        itemService.createItem(owner.getId(), hammer);

        Collection<ItemDto> result = itemService.findByNameContainingIgnoreCaseAndAvailableTrue("дрель");

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName()).isEqualTo("Электрическая Дрель");
    }

    @Test
    void shouldCreateComment() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная")
                .available(true).build();
        ItemDto item = itemService.createItem(owner.getId(), itemDto);

        User renter = User.builder()
                .name("Арендатор")
                .email("renter@example.com")
                .build();
        renter = userRep.save(renter);

        //Нужна завершённая бронь
        Booking booking = Booking.builder()
                .item(itemRep.findById(item.getId()).orElseThrow())
                .booker(renter)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();
        bookingRep.save(booking);

        //Новый комментарий
        NewCommentRequest request = new NewCommentRequest("Отличная дрель, рекомендую!");
        CommentDto comment = itemService.createComment(renter.getId(), item.getId(), request);

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getText()).isEqualTo("Отличная дрель, рекомендую!");
        assertThat(comment.getAuthorName()).isEqualTo("Арендатор");
        assertThat(comment.getCreated()).isNotNull();
    }
}
