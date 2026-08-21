package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDTO;

import java.util.Collection;


public interface UserService {

    UserDTO insertUser(UserDTO userDTO);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    UserDTO getUser(Long userId);

    void deleteUser(Long userId);

    Collection<UserDTO> getAllUsers();

}
