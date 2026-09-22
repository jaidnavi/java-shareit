package ru.practicum.shareit.item;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private ItemDTO itemDTO;

    private final Long userId = 1L;


    @BeforeEach
    void settingBeforeEach() {
        itemDTO = ItemDTO.builder()
                .id(12345L)
                .name("Костицын")
                .description("Тестовое описание вещи")
                .available(true)
                .build();
    }

    @Test
    void testCreate() throws Exception {
        Mockito.when(itemService.insertItem(any(ItemDTO.class), eq(userId)))
                .thenReturn(itemDTO);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId) // Передаем заголовок владельца
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Костицын"))
                .andExpect(jsonPath("$.description").value("Тестовое описание вещи"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void testReadItem() throws Exception {
        Mockito.when(itemService.getItem(12345L, userId))
                .thenReturn(itemDTO);

        mvc.perform(get("/items/{itemId}", 12345L)
                        .header("X-Sharer-User-Id", userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Костицын"))
                .andExpect(jsonPath("$.description").value("Тестовое описание вещи"));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito.when(itemService.updateItem(eq(12345L), any(ItemDTO.class), eq(userId)))
                .thenReturn(itemDTO);

        mvc.perform(patch("/items/{itemId}", 12345L)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Костицын"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void testSearch() throws Exception {
        Mockito.when(itemService.searchItemBySearchText("Костицын"))
                .thenReturn(List.of(itemDTO));

        mvc.perform(get("/items/search")
                        .param("text", "Костицын")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12345))
                .andExpect(jsonPath("$[0].name").value("Костицын"));
    }

    @Test
    void testGetListItemByOwner() throws Exception {

        Mockito.when(itemService.getListItemByOwner(userId))
                .thenReturn(List.of(itemDTO));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12345))
                .andExpect(jsonPath("$[0].name").value("Костицын"))
                .andExpect(jsonPath("$[0].description").value("Тестовое описание вещи"));

        Mockito.verify(itemService, Mockito.times(1)).getListItemByOwner(userId);
    }

    @Test
    void testAddCommentToItem() throws Exception {
        Long itemId = 12345L;

        CommentsDTO commentDto = new CommentsDTO();
        commentDto.setId(1L);
        commentDto.setText("Отличный инструмент, всё работает!");
        commentDto.setAuthorName("Роман");

        Mockito.when(itemService.addCommentToItem(eq(itemId), any(CommentsDTO.class), eq(userId)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)) // Сериализуем в JSON
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                // Проверяем корректность возвращенных полей комментария
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличный инструмент, всё работает!"))
                .andExpect(jsonPath("$.authorName").value("Роман"));

        Mockito.verify(itemService, Mockito.times(1))
                .addCommentToItem(eq(itemId), any(CommentsDTO.class), eq(userId));
    }

}
