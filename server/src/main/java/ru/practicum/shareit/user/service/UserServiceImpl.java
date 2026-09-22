package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;


@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDTO insertUser(UserDTO userDTO) {
        User user = userMapper.userDTOToUser(userDTO);
        User savedUser = userRepository.save(user);
        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + userId + " не найден "));

        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }

        if (user.getEmail() != null) {
            if (!user.getEmail().contains("@")) {
                throw new ValidationException("Электронная почта должна содержать символ @");
            }
        }

        return userMapper.userToUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + userId + " не найден"));
        return userMapper.userToUserDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public Collection<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDTO)
                .toList();
    }
}
