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
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ItemController.class, GlobalExceptionHandler.class})
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("unused")
    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        // Дефолтное поведение для создания вещи: возвращаем то, что прислали
        Mockito.when(itemClient.createItem(Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenAnswer(inv -> ResponseEntity.ok(inv.getArgument(1)));
    }

    @Test
    void getAllItemsShouldReturnOkWithEmptyList() throws Exception {
        Mockito.when(itemClient.getAll(1L))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchItemsShouldReturnOkWithList() throws Exception {
        ItemDto item = ItemDto.builder().id(1L).name("Дрель").description("Хорошая").available(true).build();
        Mockito.when(itemClient.searchItems(Mockito.eq("Дрель"), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(ResponseEntity.ok(List.of(item)));

        mockMvc.perform(get("/items/search")
                        .param("text", "Дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void getItemByIdShouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        Mockito.when(itemClient.getItemById(1L, 999L))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItemShouldReturnOkWhenFieldsCorrect() throws Exception {
        ItemDto newItem = ItemDto.builder().id(1L).name("Дрель").description("Хорошая").available(true).build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void createCommentShouldReturnOk() throws Exception {
        NewCommentRequest request = new NewCommentRequest("Отличная вещь!");
        Mockito.when(itemClient.createComment(Mockito.eq(1L), Mockito.eq(1L), Mockito.any(NewCommentRequest.class)))
                .thenReturn(ResponseEntity.ok("Комментарий сохранён")); // Заглушка

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItemShouldReturnOk() throws Exception {
        Mockito.when(itemClient.updateItem(Mockito.eq(1L), Mockito.eq(1L), Mockito.any()))
                .thenReturn(ResponseEntity.ok(ItemDto.builder().id(1L).name("Updated").build()));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }
}