package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends IllegalArgumentException {
    public ValidationException(String message) {
        super(message);
        log.warn(message);
    }
}
