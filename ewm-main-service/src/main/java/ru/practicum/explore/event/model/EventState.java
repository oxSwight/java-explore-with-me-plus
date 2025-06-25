package ru.practicum.explore.event.model;

public enum EventState {
    PENDING,     // событие создано, ожидает модерации
    PUBLISHED,   // опубликовано админом
    CANCELED     // отменено инициатором или админом
}