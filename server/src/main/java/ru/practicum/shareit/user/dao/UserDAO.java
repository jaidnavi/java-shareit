package ru.practicum.shareit.user.dao;


import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserDAO {
    User insert(User user);

    User update(User user);

    Optional<User> get(Long userId);

    void delete(Long userId);

    Collection<User> getUsers();
}
