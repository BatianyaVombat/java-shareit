package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.DuplicatedDataException;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Collection<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public User createUser(User user) {

        if (findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }

        User newUser = User.builder()
                .id(idCounter.getAndIncrement())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        users.put(newUser.getId(), newUser);

        return newUser;
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id пользователя обязателен");
        }

        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }

        users.put(oldUser.getId(), oldUser);
        return oldUser;

    }

    public void deleteUser(Long id) {
        User user = users.remove(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }
}
