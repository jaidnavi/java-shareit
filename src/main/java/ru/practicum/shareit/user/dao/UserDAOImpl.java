package ru.practicum.shareit.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class UserDAOImpl implements UserDAO {

    private final Map<Long, User> users = new HashMap<>();

    private long getNextUserId() {
        long currUserId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        currUserId++;
        return currUserId;
    }

    @Override
    public User insert(User user) {

        log.warn(user.getEmail());

        boolean emailExists = users.values().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExists) {
            log.warn("Пользователь с email {} уже существует", user.getEmail());
            throw new ConflictException("Пользователь с таким email уже зарегистрирован.");
        }

        user.setId(getNextUserId());

        users.put(user.getId(), user);

        log.info("Пользователь {} успешно добавлен (id = {}).", user.getName(), user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        User updatedUser = users.get(user.getId());
        if (updatedUser == null) {
            throw new NoDataFoundException("Не найден пользователь с идентификатором " + user.getId());
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            boolean emailExists = users.values().stream()
                    .anyMatch(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail())
                            && !existingUser.getId().equals(user.getId()));

            if (emailExists) {
                log.warn("Не удалось обновить: email {} уже занят другим пользователем", user.getEmail());
                throw new ConflictException("Этот email уже занят другим пользователем.");
            }
            updatedUser.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            updatedUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            updatedUser.setEmail(user.getEmail());
        }

        log.info("Пользователь с id = {} успешно обновлен.", user.getId());
        return updatedUser;
    }

    @Override
    public Optional<User> get(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void delete(Long userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
            log.info("Пользователь c идентификатором {} успешно удален.", userId);
        } else {
            throw new NoDataFoundException("Не найден пользователь с идентификатором " + userId);
        }
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

}
