package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.GlobalExceptionHandler;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ItemRequestController.class, GlobalExceptionHandler.class})
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("unused")
    private ItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        Mockito.when(itemRequestClient.createRequest(Mockito.anyLong(), Mockito.any(ItemRequestDto.class)))
                .thenAnswer(inv -> {
                    ItemRequestDto req = inv.getArgument(1);
                    req.setId(1L);
                    req.setCreated(LocalDateTime.now());
                    return ResponseEntity.ok(req);
                });
    }

    @Test
    void createRequestShouldReturnOkWhenTextIsValid() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder().description("Мне нужна дрель!").build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Мне нужна дрель!"));
    }

    @Test
    void getOwnRequestsShouldReturnOkWithList() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder().id(1L).description("Мне нужна дрель!").build();
        Mockito.when(itemRequestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(request)));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Мне нужна дрель!"));
    }

    @Test
    void getOtherRequestsShouldReturnOkWithList() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder().id(2L).description("Нужен молоток").build();
        Mockito.when(itemRequestClient.getOtherRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(request)));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Нужен молоток"));
    }

    @Test
    void getRequestByIdShouldReturnOkWithoutHeader() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder().id(5L).description("Нужна фотокамера").build();
        Mockito.when(itemRequestClient.getRequestById(5L))
                .thenReturn(ResponseEntity.ok(request));

        mockMvc.perform(get("/requests/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("Нужна фотокамера"));
    }

    @Test
    void getRequestByIdShouldReturnNotFoundWhenRequestDoesNotExist() throws Exception {
        Mockito.when(itemRequestClient.getRequestById(999L))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/requests/999"))
                .andExpect(status().isNotFound());
    }
}