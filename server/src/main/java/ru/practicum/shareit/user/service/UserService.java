package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDTO;

import java.util.Collection;

/**
 * Интерфейс функционала для объекта пользователь
 */
public interface UserService {

    /**
     * Метод добавления пользователя
     *
     * @param userDTO данные нового пользователя
     * @return объект новый пользователь
     */
    UserDTO insertUser(UserDTO userDTO);

    /**
     * Метод изменения пользователя
     *
     * @param userId  идентификатор пользователя
     * @param userDTO данные пользователя для изменения
     * @return объект измененный пользователь
     */
    UserDTO updateUser(Long userId, UserDTO userDTO);

    /**
     * Метод получения пользователя
     *
     * @param userId идентификатор пользователя
     * @return объект пользователь
     */
    UserDTO getUser(Long userId);

    /**
     * Метод удаления пользователя
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);

    /**
     * Метод получения списка всех пользователей
     *
     * @return список всех пользователей
     */
    Collection<UserDTO> getAllUsers();

}
