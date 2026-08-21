package ru.practicum.shareit.user.mapping;

import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.model.User;

public class UserMapper {
    public static UserDTO toUserDto(User user) {
        return new UserDTO(
                user.getName(),
                user.getEmail()
        );
    }
}
