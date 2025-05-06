package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }


    public User createUser(User user) {
        if (user.getEmail().isEmpty() || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        users.values().stream()
                .filter(elem -> user.getEmail().equals(elem.getEmail()))
                .findFirst()
                .ifPresent(elem -> {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                });
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        users.values().stream()
                .filter(elem -> newUser.getEmail().equals(elem.getEmail()))
                .findFirst()
                .ifPresent(elem -> {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                });
        User oldUser = users.get(newUser.getId());
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getUsername() != null) {
            oldUser.setUsername(newUser.getUsername());
        }
        if (newUser.getPassword() != null) {
            oldUser.setPassword(newUser.getPassword());
        }
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Optional<User> findUserById(Long id) {
        return users.values()
                .stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    public User findUser(Long id) {
        return users.values()
                .stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ConditionsNotMetException("Пользователь с id " + id + " не найден"));
    }
}
