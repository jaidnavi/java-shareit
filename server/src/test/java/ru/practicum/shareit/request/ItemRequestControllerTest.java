package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDTO;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDTO requestDTO;
    private final Long userId = 1L;
    private final Long requestId = 10L;

    @BeforeEach
    void settingBeforeEach() {
        requestDTO = ItemRequestDTO.builder()
                .id(requestId)
                .description("Нужна мощная дрель для ремонта")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void testAddNewRequest() throws Exception {
        Mockito.when(itemRequestService.addNewRequest(any(ItemRequestDTO.class), eq(userId)))
                .thenReturn(requestDTO);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDTO))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна мощная дрель для ремонта"));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .addNewRequest(any(ItemRequestDTO.class), eq(userId));
    }

    @Test
    void testGetItemRequests() throws Exception {
        // Логика контроллера: вызывает getItemRequests(ownerId, false)
        Mockito.when(itemRequestService.getItemRequests(userId, false))
                .thenReturn(List.of(requestDTO));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Нужна мощная дрель для ремонта"));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getItemRequests(userId, false);
    }

    @Test
    void testGetAllItemRequests() throws Exception {
        // Логика контроллера: вызывает getItemRequests(ownerId, true)
        Mockito.when(itemRequestService.getItemRequests(userId, true))
                .thenReturn(List.of(requestDTO));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getItemRequests(userId, true);
    }

    @Test
    void testGetItemRequestById() throws Exception {
        Mockito.when(itemRequestService.getItemRequestById(requestId))
                .thenReturn(requestDTO);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна мощная дрель для ремонта"));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getItemRequestById(requestId);
    }
}