package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.user.dao.UserDAO;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;


@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserDAO userDAO, UserMapper userMapper) {
        this.userDAO = userDAO;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO insertUser(UserDTO userDTO) {

        User user = userMapper.userDTOToUser(userDTO);

        User savedUser = userDAO.insert(user);

        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {

        User user = userMapper.userDTOToUser(userDTO);

        user.setId(userId);

        User updatedItem = userDAO.update(user);

        return userMapper.userToUserDTO(updatedItem);
    }

    @Override
    public UserDTO getUser(Long userId) {
        User user = userDAO.get(userId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + userId + " не найден"));

        return userMapper.userToUserDTO(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userDAO.delete(userId);
    }

    @Override
    public Collection<UserDTO> getAllUsers() {
        return userDAO.getUsers().stream()
                .map(userMapper::userToUserDTO)
                .toList();
    }
}
