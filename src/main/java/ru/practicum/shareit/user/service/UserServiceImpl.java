package ru.practicum.shareit.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRep;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        User created = userRep.createUser(user);
        return UserMapper.toResponse(created);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRep.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return UserMapper.toResponse(user);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        request.setId(id);
        User user = UserMapper.toEntity(request);

        if (user.getEmail() != null) {
            userRep.findByEmail(user.getEmail())
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> {
                        throw new ValidationException("Email уже используется");
                    });
        }

        User updated = userRep.updateUser(user);
        return UserMapper.toResponse(updated);
    }

    @Override
    public void deleteUser(Long userId) {
        userRep.deleteUser(userId);
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRep.getAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }
}
