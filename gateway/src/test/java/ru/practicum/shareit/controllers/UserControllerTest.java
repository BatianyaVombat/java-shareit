package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.GlobalExceptionHandler;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("unused")
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        Mockito.when(userClient.createUser(Mockito.any(UserDto.class)))
                .thenAnswer(inv -> ResponseEntity.ok(inv.getArgument(0)));
    }

    @Test
    void getAllUsersShouldReturnOkWithEmptyList() throws Exception {
        //имитация пустого списка пользователей
        List<UserDto> emptyList = List.of();

        Mockito.when(userClient.getAll())
                .thenReturn(ResponseEntity.ok(emptyList));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk()) //код ответа 200
                .andExpect(jsonPath("$").isArray()) //массив значений
                .andExpect(jsonPath("$.length()").value(0)); //нулевой размер
    }

    @Test
    void getAllUsersShouldReturnListOfUsersWhenTheyExist() throws Exception {
        // Список с одним фейковым пользователем
        UserDto userDto = UserDto.builder().id(1L).name("Иван").email("ivan@test.com").build();
        List<UserDto> users = List.of(userDto);

        Mockito.when(userClient.getAll())
                .thenReturn(ResponseEntity.ok(users));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Массив длины 1
                .andExpect(jsonPath("$[0].id").value(1))    // Проверяем поля первого элемента
                .andExpect(jsonPath("$[0].name").value("Иван"))
                .andExpect(jsonPath("$[0].email").value("ivan@test.com"));
    }

    @Test
    void createUserShouldReturnOkWhenFieldsCorrect() throws Exception {
        UserDto savedUser = UserDto.builder().id(1L).name("Иван").email("test@email.com").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateUserShouldReturnOk() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("test@email.com")
                .build();

        Mockito.when(userClient.updateUser(Mockito.eq(userId), Mockito.any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok(UserDto.builder().id(userId).name("Иван").email("test@email.com")
                        .build()));

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByIdShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        Long userId = 999L;

        Mockito.when(userClient.getUserById(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        Long userId = 999L;

        Mockito.when(userClient.deleteUser(userId))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNotFound());
    }
}
