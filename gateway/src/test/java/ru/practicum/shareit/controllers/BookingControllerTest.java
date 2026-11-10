package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BookingController.class, GlobalExceptionHandler.class})
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("unused")
    private BookingClient bookingClient;

    @Test
    void getBookingsShouldReturnOkWhenStateIsValid() throws Exception {
        // Мокаем ответ клиента (пустой список)
        Mockito.when(bookingClient.getBookings(Mockito.eq(1L), Mockito.eq("ALL")))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBookingsShouldReturnBadRequestWhenStateIsInvalid() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItemShouldReturnOkWhenDatesAreValid() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Mockito.when(bookingClient.bookItem(Mockito.eq(1L), Mockito.any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok("Бронирование создано"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingByIdShouldReturnOk() throws Exception {
        Mockito.when(bookingClient.getBooking(1L, 100L))
                .thenReturn(ResponseEntity.ok("Информация по брони"));

        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void approveBookingShouldReturnOkWhenApproved() throws Exception {
        Mockito.when(bookingClient.approveBooking(1L, 100L, true))
                .thenReturn(ResponseEntity.ok("Подтверждено"));

        mockMvc.perform(patch("/bookings/100")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwnerShouldReturnOk() throws Exception {
        Mockito.when(bookingClient.getBookingsByOwner(Mockito.eq(1L), Mockito.eq("ALL")))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBookingsByOwnerShouldReturnBadRequestWhenStateIsInvalid() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "WRONG"))
                .andExpect(status().isBadRequest());
    }
}