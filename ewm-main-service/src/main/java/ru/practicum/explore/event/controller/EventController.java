package ru.practicum.explore.event.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;
import ru.practicum.explore.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    /* ---------- приватный API пользователя ---------- */

    /** GET / users/{userId}/events/{eventId} */
    @GetMapping("/users/{userId}/events/{eventId}")
    public EventDto getEventById(@PathVariable @Positive long userId,
                                 @PathVariable @Positive long eventId) {

        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getEventById(userId, eventId);
    }

    /** GET / users/{userId}/events */
    @GetMapping("/users/{userId}/events")
    public Collection<ResponseEventDto> getUserEvents(
            @PathVariable @Positive long userId,
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        log.info("GET /users/{}/events  from={} size={}", userId, from, size);
        return eventService.getAllUserEvents(userId, from, size);
    }

    /** PATCH / users/{userId}/events/{eventId} */
    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventDto changeEvent(
            @PathVariable @Positive long userId,
            @PathVariable @Positive long eventId,
            @RequestBody PatchEventDto patchEventDto) {

        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.changeEvent(userId, eventId, patchEventDto);
    }

    /** POST / users/{userId}/events */
    @PostMapping("/users/{userId}/events")
    public EventDto createEvent(@PathVariable @Positive long userId,
                                @RequestBody PatchEventDto newEventDto) {

        log.info("POST /users/{}/events", userId);
        return eventService.createEvent(userId, newEventDto);
    }


    @GetMapping("/events/{id}")
    public EventDto getPublishedEventById(@PathVariable @Positive long id) {
        log.info("GET /events/{}", id);
        return eventService.getPublishedEventById(id);
    }

    /** GET / events — поиск опубликованных */
    @GetMapping("/events")
    public Collection<ResponseEventDto> searchEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        log.info("GET /events  text={} categories={} paid={} …", text, categories, paid);
        return eventService.findEventsByUser(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    /* ---------- административный API ---------- */

    /** GET / admin/events */
    @GetMapping("/admin/events")
    public Collection<EventDto> findEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        log.info("GET /admin/events");
        return eventService.findEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    /** PATCH / admin/events/{eventId} */
    @PatchMapping("/admin/events/{eventId}")
    public EventDto changeEventByAdmin(@PathVariable @Positive long eventId,
                                       @RequestBody PatchEventDto patchEventDto) {

        log.info("PATCH /admin/events/{}", eventId);
        return eventService.changeEventByAdmin(eventId, patchEventDto);
    }
}
