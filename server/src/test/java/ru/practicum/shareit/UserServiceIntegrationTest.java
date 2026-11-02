package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUserAndFindItById() {
        UserDto createDto = UserDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        UserDto created = userService.createUser(createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Иван");

        UserDto found = userService.getUserById(created.getId());

        //Проверяем маппинг
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Иван");
        assertThat(found.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + 999L + " не найден");
    }

    @Test
    void shouldUpdateExistingUser() {
        UserDto newUserDto = UserDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        UserDto created = userService.createUser(newUserDto);
        UpdateUserRequest updateData = UpdateUserRequest.builder()
                .name("Мария")
                .email("mary@example.com")
                .build();

        UserDto updated = userService.updateUser(created.getId(), updateData);
        assertThat(updated.getName()).isEqualTo("Мария");
        assertThat(updated.getEmail()).isEqualTo("mary@example.com");
    }

    @Test
    void shouldDeleteExistingUser() {
        UserDto createDto = UserDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        UserDto created = userService.createUser(createDto);
        assertThat(userService.getUserById(created.getId())).isNotNull();

        userService.deleteUser(created.getId());
        assertThatThrownBy(() -> userService.getUserById(created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + created.getId() + " не найден");
    }

    @Test
    void shouldReturnListOfAllUsers() {
        //проверяем что возвращается пустой список, если пользователей ещё нет
        Collection<UserDto> userDtoCollection = userService.getAll();
        assertThat(userDtoCollection.size()).isEqualTo(0);

        UserDto newUserOne = UserDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        UserDto newUserTwo = UserDto.builder()
                .name("Мария")
                .email("mary@example.com")
                .build();

        userService.createUser(newUserOne);
        userService.createUser(newUserTwo);

        userDtoCollection = userService.getAll();
        assertThat(userDtoCollection.size()).isEqualTo(2);

        assertThat(userDtoCollection)
                .hasSize(2)
                .extracting(UserDto::getName)
                .containsExactlyInAnyOrder("Иван", "Мария");
    }
}