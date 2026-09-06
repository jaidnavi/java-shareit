package ru.practicum.shareit.user.mapping;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class UserMapper {
    public User userDTOToUser(UserDTO userDTO) {
        return User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .build();
    }

    public UserDTO userToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public Collection<UserDTO> userToUserDTOCollection(Collection<User> users) {
        if (users == null) {
            return null;
        }

        Collection<UserDTO> collection = new ArrayList<>(users.size());
        for (User user : users) {
            collection.add(userToUserDTO(user));
        }
        return collection;
    }
}
