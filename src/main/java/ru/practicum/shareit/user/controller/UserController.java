package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDTO add(@Valid @RequestBody UserDTO user) {
        return userService.insertUser(user);
    }

    @PatchMapping("/{userId}")
    public UserDTO update(@Valid @PathVariable Long userId,
                          @RequestBody UserDTO userDTO) {
        return userService.updateUser(userId, userDTO);
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@Valid @PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@Valid @PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping
    public Collection<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }
}
