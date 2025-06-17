package ru.practicum.explore.event.service;

import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {
    EventDto getEventById(long userId, long eventId);

    Collection<ResponseEventDto> getAllUserEvents(long userId, Integer from, Integer size);

    EventDto changeEvent(long userId, long eventId, PatchEventDto patchEventDto);

    EventDto createEvent(long userId, PatchEventDto newEventDto);

    EventDto getPublishedEventById(long eventId, Integer views);

    Collection<ResponseEventDto> findEventsByUser(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventDto changeEventByAdmin(long eventId, PatchEventDto patchEventDto);

    Collection<EventDto> findEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);
}