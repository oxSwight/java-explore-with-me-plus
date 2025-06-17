package ru.practicum.explore.event.controller;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;
import ru.practicum.explore.event.service.EventService;
import ru.practicum.explore.global.service.ExchangeService;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventController {

    private final EventService eventService;

    @GetMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Request to get event with ID {} of user with ID {} received.", eventId, userId);
        return ResponseEntity.ok().body(eventService.getEventById(userId, eventId));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventDto> getPublishedEventById(@PathVariable long id, @RequestParam Integer views) {
        log.info("Request to get published event with ID {} received.", id);
        return ResponseEntity.ok().body(eventService.getPublishedEventById(id, views));
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<Collection<ResponseEventDto>> getEvents(@PathVariable long userId, @RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10") Integer size) {
        log.info("Request to get events of the user received.");
        return ResponseEntity.ok().body(eventService.getAllUserEvents(userId, from, size));
    }

    @GetMapping("/admin/events")
    public ResponseEntity<Collection<EventDto>> findEventsByAdmin(@RequestParam List<Long> users, @RequestParam List<String> states, @RequestParam List<Long> categories, @PastOrPresent @RequestParam LocalDateTime rangeStart, @FutureOrPresent @RequestParam LocalDateTime rangeEnd, @RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10") Integer size) {
        log.info("Request to get events by Admin received.");
        return ResponseEntity.ok().body(eventService.findEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @GetMapping("/events")
    public ResponseEntity<Collection<ResponseEventDto>> findEventsByUser(@RequestParam(required = false) String text, @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) Boolean paid, @PastOrPresent @RequestParam(required = false) LocalDateTime rangeStart, @FutureOrPresent @RequestParam(required = false) LocalDateTime rangeEnd, @RequestParam(required = false) Boolean onlyAvailable, @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10") Integer size) {
        log.info("Request to get events by User received.");
        return ResponseEntity.ok().body(eventService.findEventsByUser(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size));
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> changeEvent(@PathVariable long userId, @PathVariable long eventId, @RequestBody PatchEventDto patchEventDto) {
        log.info("Request to change event {} of user with ID {} received.", eventId, userId);
        return ResponseEntity.ok().body(eventService.changeEvent(userId, eventId, patchEventDto));
    }

    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventDto> changeEventByAdmin(@PathVariable long eventId, @RequestBody PatchEventDto patchEventDto) {
        log.info("Request to change event with ID {} received.", eventId);
        return ResponseEntity.ok().body(eventService.changeEventByAdmin(eventId, patchEventDto));
    }

    @PostMapping("/users/{userId}/events")
    public ResponseEntity<EventDto> createEvent(@PathVariable long userId, @RequestBody PatchEventDto newEventDto) throws IOException {
        log.info("Request to create new event received: {}", newEventDto);
        EventDto event = eventService.createEvent(userId, newEventDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(event.getId()).toUri();
        log.info("New user created with ID {}", event.getId());
        return ResponseEntity.created(location).headers(ExchangeService.exchange(event)).body(event);
    }
}