package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserById(Long userId);

    UserDto updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long userId);

    Collection<UserDto> getAll();
}
