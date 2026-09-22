package ru.practicum.shareit.booking.enumeration;

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

}