package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRep;

    public UserDto createUser(UserDto userDto) {
        User newUser = UserMapper.toEntity(userDto);
        newUser.setId(null);

        return UserMapper.toResponse(userRep.save(newUser));
    }

    public UserDto getUserById(Long id) {
        User findedUser = userRep.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return UserMapper.toResponse(findedUser);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User oldUser = userRep.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));

        if (request.getName() != null) {
            oldUser.setName(request.getName());
        }
        if (request.getEmail() != null) {
            oldUser.setEmail(request.getEmail());
        }

        return UserMapper.toResponse(userRep.save(oldUser));
    }

    public void deleteUser(Long userId) {
        if (!userRep.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        userRep.deleteById(userId);
    }

    public Collection<UserDto> getAll() {
        return userRep.findAll().stream().map(UserMapper::toResponse).toList();
    }
}
