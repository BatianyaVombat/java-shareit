package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;

import static ru.practicum.shareit.booking.enums.StateFilterStatus.*;

import ru.practicum.shareit.booking.enums.StateFilterStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRep;
    private final UserRepository userRep;
    private final ItemRepository itemRep;

    @Transactional
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRep.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + bookingDto.getItemId() + " не найдена"));

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата окончания бронирования не может быть в прошлом");
        }

        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования должна быть раньше даты окончания");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        Booking booking = BookingMapper.toEntity(bookingDto, booker, item);
        booking.setStatus(BookingStatus.WAITING);

        Booking created = bookingRep.save(booking);
        return BookingMapper.toResponse(created);
    }

    @Transactional
    public BookingDto approvedBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRep.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Подтверждать бронирование имеет право только владелец вещи");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование не ожидает подтверждения");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponse(bookingRep.save(booking));
    }

    public Collection<BookingDto> getBookingsByBooker(Long userId, String state) {
        userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Collection<Booking> bookingList = bookingRep.findAllByBookerId(userId);
        Collection<Booking> filtered = stateFilter(bookingList, state);

        return filtered.stream()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());

    }

    public Collection<BookingDto> getBookingsByOwner(Long userId, String state) {
        userRep.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Collection<Booking> bookings = bookingRep.findAllByOwnerId(userId);

        Collection<Booking> filtered = stateFilter(bookings, state);

        return filtered.stream()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRep.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование с id = " + bookingId + " не найдено");
        }

        return BookingMapper.toResponse(booking);
    }

    private Collection<Booking> stateFilter(Collection<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();

        StateFilterStatus status = ALL;
        if (state != null) {
            try {
                status = StateFilterStatus.valueOf(state.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Неизвестный state " + state);
            }
        }

        return switch (status) {
            case ALL -> bookings;
            case CURRENT -> bookings.stream()
                    .filter(b -> !b.getStart().isAfter(now) && !b.getEnd().isBefore(now))
                    .collect(Collectors.toList());
            case PAST -> bookings.stream().filter(b -> b.getEnd().isBefore(now)).collect(Collectors.toList());
            case FUTURE -> bookings.stream().filter(b -> b.getStart().isAfter(now)).collect(Collectors.toList());
            case WAITING -> bookings.stream().filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                    .collect(Collectors.toList());
            case REJECTED -> bookings.stream().filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                    .collect(Collectors.toList());
        };

    }
}
