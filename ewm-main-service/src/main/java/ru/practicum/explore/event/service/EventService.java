package ru.practicum.explore.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.NewEventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {

    EventDto getEventById(long userId, long eventId);

    EventDto getPublishedEventById(long eventId);

    EventDto getPublishedEventById(long eventId, Integer views);

    Collection<ResponseEventDto> getAllUserEvents(long userId, Integer from, Integer size);

    ResponseEventDto changeEvent(long userId, long eventId, PatchEventDto patchEventDto);

    EventDto createEvent(long userId, PatchEventDto newEventDto);

    Collection<ResponseEventDto> findEventsByUser(String text,
                                                  List<Long> categories,
                                                  Boolean paid,
                                                  LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable,
                                                  String sort,
                                                  Integer from,
                                                  Integer size);

    ResponseEventDto changeEventByAdmin(long eventId, PatchEventDto patchEventDto);

    Collection<ResponseEventDto> findEventsByAdmin(List<Long> users,
                                                   List<String> states,
                                                   List<Long> categories,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   Integer from,
                                                   Integer size);

    Collection<ResponseEventDto> getUserEvents(long userId, int from, int size);

    ResponseEventDto getUserEventById(long userId, long eventId);

    Collection<ResponseEventDto> findEvents(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd,
                                            Boolean onlyAvailable,
                                            String sort,
                                            Integer from,
                                            Integer size,
                                            HttpServletRequest request);

    ResponseEventDto getPublicEvent(long eventId, HttpServletRequest request);

    Collection<ResponseEventDto> findAdminEvents(List<Long> users,
                                                 List<String> states,
                                                 List<Long> categories,
                                                 LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd,
                                                 Integer from,
                                                 Integer size);

    ResponseEventDto createEvent(long userId, NewEventDto newEventDto);
}
