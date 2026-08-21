package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.user.dto.UserDTO;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShareItAppTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void postUser_whenCorrectData_addUser() {
        UserDTO user = UserDTO.builder()
                .email("test_" + UUID.randomUUID() + "@yandex.ru") // Уникальный email во избежание коллизий
                .name("kosticin")
                .build();

        ResponseEntity<UserDTO> postResponse = restTemplate.postForEntity("/users", user, UserDTO.class);
        assertEquals(200, postResponse.getStatusCode().value());
        UserDTO createdUser = postResponse.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("kosticin", createdUser.getName());
    }

    @Test
    void postUser_whenInvalidData_Error() {
        UserDTO user = UserDTO.builder()
                .email("invalid-email-format") // Некорректный email
                .name("kosticin")
                .build();
        ResponseEntity<UserDTO> postResponse = restTemplate.postForEntity("/users", user, UserDTO.class);
        assertEquals(400, postResponse.getStatusCode().value());
    }

    @Test
    void getUsers() {
        String uniqueName = "login_" + System.currentTimeMillis();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@yandex.ru";

        UserDTO user = UserDTO.builder()
                .email(uniqueEmail)
                .name(uniqueName)
                .build();

        ResponseEntity<UserDTO> postResponse = restTemplate.postForEntity("/users", user, UserDTO.class);
        assertEquals(200, postResponse.getStatusCode().value());

        ResponseEntity<UserDTO[]> getResponse = restTemplate.getForEntity("/users", UserDTO[].class);
        assertEquals(200, getResponse.getStatusCode().value());
        UserDTO[] getUsers = getResponse.getBody();
        assertNotNull(getUsers);
        assertTrue(getUsers.length > 0, "Список не должен быть пустым");
    }

    @Test
    void patchUser_whenCorrectData_updateUser() {
        UserDTO initialUser = UserDTO.builder()
                .email("old_" + UUID.randomUUID() + "@yandex.ru")
                .name("Старое Имя")
                .build();
        ResponseEntity<UserDTO> postResponse = restTemplate.postForEntity("/users", initialUser, UserDTO.class);
        assertEquals(200, postResponse.getStatusCode().value());
        UserDTO savedUser = postResponse.getBody();
        assertNotNull(savedUser);
        Long userId = savedUser.getId();

        UserDTO updatedData = UserDTO.builder()
                .email("new_" + UUID.randomUUID() + "@yandex.ru")
                .name("Новое Имя")
                .build();

        ResponseEntity<UserDTO> patchResponse = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.PATCH,
                new HttpEntity<>(updatedData),
                UserDTO.class
        );

        assertEquals(200, patchResponse.getStatusCode().value());
        UserDTO updatedUser = patchResponse.getBody();
        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId(), "ID пользователя не должен измениться");
        assertEquals(updatedData.getEmail(), updatedUser.getEmail());
        assertEquals("Новое Имя", updatedUser.getName());
    }

    @Test
    void putUser_whenIncorrectID_getError() {
        UserDTO updatedData = UserDTO.builder()
                .id(999L)
                .email("new_email@yandex.ru")
                .name("Новое Имя")
                .build();

        ResponseEntity<UserDTO> patchResponse = restTemplate.exchange(
                "/users/999",
                HttpMethod.PATCH,
                new HttpEntity<>(updatedData),
                UserDTO.class
        );

        assertEquals(404, patchResponse.getStatusCode().value());
    }


    @Test
    void postItem_whenCorrectData_addItem() {

        // Создаем пользователя и динамически получаем его ID
        UserDTO user = UserDTO.builder()
                .email("owner_" + UUID.randomUUID() + "@yandex.ru")
                .name("kosticin222")
                .build();

        ResponseEntity<UserDTO> userResponse = restTemplate.postForEntity("/users", user, UserDTO.class);
        assertEquals(200, userResponse.getStatusCode().value());
        assertNotNull(userResponse.getBody());
        Long ownerId = userResponse.getBody().getId();

        ItemDTO itemDto = ItemDTO.builder()
                .name("ФЫВА")
                .description("ОЛДЖ")
                .available(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(ownerId)); // Передаем реальный ID созданного юзера
        HttpEntity<ItemDTO> requestEntity = new HttpEntity<>(itemDto, headers);

        ResponseEntity<ItemDTO> itemResponse = restTemplate.exchange(
                "/items",
                HttpMethod.POST,
                requestEntity,
                ItemDTO.class
        );

        assertEquals(200, itemResponse.getStatusCode().value());
        ItemDTO createdItem = itemResponse.getBody();
        assertNotNull(createdItem);
        assertNotNull(createdItem.getId(), "ID предмета должен быть сгенерирован");
        assertEquals("ФЫВА", createdItem.getName());

    }

    @Test
    void postItem_whenInvalidData_Error() {
        ItemDTO itemDto = ItemDTO.builder()
                .name("ФЫВА")
                .description("ОЛДЖ")
                .available(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "999999"); // Несуществующий владелец
        HttpEntity<ItemDTO> requestEntity = new HttpEntity<>(itemDto, headers);

        ResponseEntity<ItemDTO> postResponse = restTemplate.exchange(
                "/items",
                HttpMethod.POST,
                requestEntity,
                ItemDTO.class
        );

        assertEquals(404, postResponse.getStatusCode().value());

    }


}