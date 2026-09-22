package ru.practicum.shareit.booking.enumeration;

import java.util.Optional;

public enum StateEnum {
    ALL("все"),
    CURRENT("текущие"),
    PAST("завершённые"),
    FUTURE("будущие"),
    WAITING("ожидающие подтверждения"),
    REJECTED("отклонённые");

    private final String description;

    StateEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<StateEnum> fromString(String stringState) {
        if (stringState == null) {
            return Optional.of(ALL);
        }
        try {
            return Optional.of(StateEnum.valueOf(stringState.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}