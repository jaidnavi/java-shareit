package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDTO;
import ru.practicum.shareit.request.mapping.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemReqRep;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDTO addNewRequest(ItemRequestDTO itemRequestDto, Long userId) {
        if (itemRequestDto == null) {
            throw new ValidationException("Не заполнено тело запроса!");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + userId + " не найден"));
        ItemRequest itemRequest = itemRequestMapper.itemRequestDTOToItemRequest(itemRequestDto);
        itemRequest.setUser(user);
        return itemRequestMapper.itemRequestToItemRequestDTO(itemReqRep.save(itemRequest), getAddedItems(List.of(itemRequest)));
    }

    @Override
    public List<ItemRequestDTO> getItemRequests(Long ownerId, boolean all) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoDataFoundException("Пользователь с id " + ownerId + " не найден"));

        List<ItemRequest> itemRequests = (all) ? itemReqRep.findByUserIdNotOrderByCreationDateDesc(ownerId) :
                itemReqRep.findByUserIdOrderByCreationDateDesc(ownerId);
        return itemRequests.stream()
                .map(itemRequest -> itemRequestMapper.itemRequestToItemRequestDTO(itemRequest, getAddedItems(List.of(itemRequest))))
                .toList();
    }

    @Override
    public ItemRequestDTO getItemRequestById(Long requestId) {
        ItemRequest itemRequest = itemReqRep.findById(requestId).orElseThrow(() ->
                new ValidationException("Запрос с id " + requestId + "не найден"));
        return itemRequestMapper.itemRequestToItemRequestDTO(itemRequest, getAddedItems(List.of(itemRequest)));
    }

    /**
     * Метод для получения списка вещей
     *
     * @param itemRequests - список запросов для которых нужно получить этот список
     * @return список вещей по указанным запросам
     */
    private List<Item> getAddedItems(List<ItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .toList();
        return itemRepository.findAllByRequestIdIn(requestIds);
    }

}
