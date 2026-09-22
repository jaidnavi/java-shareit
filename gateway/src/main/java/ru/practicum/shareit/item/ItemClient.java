package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentsDTO;
import ru.practicum.shareit.item.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;

@Service
@Slf4j
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> insertItem(ItemDTO itemDTO, Long ownerId) {
        return post("", ownerId, itemDTO);
    }


    public ResponseEntity<Object> updateItem(Long itemId, ItemDTO itemDTO, Long ownerId) {
        return patch("/" + itemId, ownerId, itemDTO);
    }

    public ResponseEntity<Object> getItem(Long itemId, Long ownerId) {
        return get("/" + itemId, ownerId);
    }


    public ResponseEntity<Object> getListItemByOwner(Long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> searchItemBySearchText(String text, Long ownerId) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", ownerId, parameters);
    }

    public ResponseEntity<Object> addCommentToItem(Long itemId, CommentsDTO commentsDTO, Long authorId) {
        return post("/" + itemId + "/comment",authorId,commentsDTO);
    }

}
