package ru.practicum.shareit.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    private UserDTO userDTO;

    @BeforeEach
    void settingBeforeEach() {
        userDTO = UserDTO.builder()
                .id(12345L)
                .name("Костицын")
                .email("kosticin@yandex.ru")
                .build();
    }

    @Test
    void testCreate() throws Exception {
        Mockito.when(userService.insertUser(any(UserDTO.class)))
                .thenReturn(userDTO);
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Костицын"))
                .andExpect(jsonPath("$.email").value("kosticin@yandex.ru"));
    }

    @Test
    void testReadUser() throws Exception {
        Mockito.when(userService.getUser(12345L))
                .thenReturn(userDTO);
        mvc.perform(get("/users/{userId}", 12345L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Костицын"))
                .andExpect(jsonPath("$.email").value("kosticin@yandex.ru"));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito.when(userService.updateUser(eq(12345L), any(UserDTO.class)))
                .thenReturn(userDTO);
        mvc.perform(patch("/users/{userId}", 12345L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Костицын"))
                .andExpect(jsonPath("$.email").value("kosticin@yandex.ru"));
    }

    @Test
    void testDelete() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(12345L);
        mvc.perform(delete("/users/{userId}", 12345L))
                .andExpect(status().isOk());
    }
}
