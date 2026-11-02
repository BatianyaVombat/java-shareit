package ru.practicum.shareit;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRep;

    @Autowired
    private ItemRepository itemRep;

    @Autowired
    private BookingRepository bookingRep;

    @Autowired
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRep.save(User.builder().name("Владелец").email("owner@mail.com").build());
        booker = userRep.save(User.builder().name("Арендатор").email("booker@mail.com").build());

        ItemDto itemDto = ItemDto.builder().name("Дрель").description("Мощная").available(true).build();
        ItemDto created = itemService.createItem(owner.getId(), itemDto);
        item = itemRep.findById(created.getId()).orElseThrow();
    }

    @Test
    void shouldCreateBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto created = bookingService.createBooking(booker.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void shouldThrowWhenBookingUnavailableItem() {
        item.setAvailable(false);
        itemRep.save(item);

        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("недоступна");
    }

    @Test
    void shouldApproveBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingDto created = bookingService.createBooking(booker.getId(), dto);

        BookingDto approved = bookingService.approvedBooking(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void shouldThrowWhenApprovingByNotOwner() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingDto created = bookingService.createBooking(booker.getId(), dto);

        assertThatThrownBy(() -> bookingService.approvedBooking(booker.getId(), created.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("только владелец");
    }

    @Test
    void shouldGetBookingsByBooker() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(booker.getId(), dto);

        Collection<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), "ALL");

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.iterator().next().getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void shouldGetBookingsByOwner() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(booker.getId(), dto);

        Collection<BookingDto> result = bookingService.getBookingsByOwner(owner.getId(), "ALL");

        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenGettingBookingByWrongUser() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingDto created = bookingService.createBooking(booker.getId(), dto);

        // Третий пользователь, который не имеет отношения к брони
        User stranger = userRep.save(User.builder().name("Чужой").email("stranger@mail.com").build());

        assertThatThrownBy(() -> bookingService.getBookingById(created.getId(), stranger.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    //----------Тестирование StateFilter----------
    @Test
    void shouldGetBookingsByBookerWithStateCurrent() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build();
        bookingRep.save(booking);

        Collection<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), "CURRENT");
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldGetBookingsByBookerWithStatePast() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();
        bookingRep.save(booking);

        Collection<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), "PAST");
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldGetBookingsByBookerWithStateFuture() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(booker.getId(), dto);

        Collection<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), "FUTURE");
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldGetBookingsByBookerWithStateWaiting() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(booker.getId(), dto); // статус WAITING по умолчанию

        Collection<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), "WAITING");
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldGetBookingsByBookerWithStateRejected() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingDto created = bookingService.createBooking(booker.getId(), dto);
        bookingService.approvedBooking(owner.getId(), created.getId(), false); // Отклоняем

        Collection<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), "REJECTED");
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenStateIsInvalid() {
        assertThatThrownBy(() -> bookingService.getBookingsByBooker(booker.getId(), "INVALID_STATE"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Неизвестный state");
    }

    //----------Валидация дат----------
    @Test
    void shouldThrowWhenStartIsInPast() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusDays(1)) // В прошлом
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("в прошлом");
    }

    @Test
    void shouldThrowWhenEndIsBeforeStart() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1)) // Конец раньше начала
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("раньше даты окончания");
    }
}
