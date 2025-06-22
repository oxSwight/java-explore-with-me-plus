package ru.practicum.explore.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.common.exception.BadRequestException;
import ru.practicum.explore.event.dto.NewEventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;
import ru.practicum.explore.event.service.EventService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping
public class EventController {

    private final EventService eventService;

    @PostMapping("/users/{userId}/events")
    public ResponseEntity<ResponseEventDto> createEvent(@PathVariable long userId,
                                                        @RequestBody @Valid NewEventDto newEventDto) {

        ResponseEventDto created = eventService.createEvent(userId, newEventDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<Collection<ResponseEventDto>> getUserEvents(@PathVariable long userId,
                                                                      @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                                      @RequestParam(defaultValue = "10") @Positive Integer size) {

        return ResponseEntity.ok(eventService.getUserEvents(userId, from, size));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<ResponseEventDto> getUserEvent(@PathVariable long userId,
                                                         @PathVariable long eventId) {

        return ResponseEntity.ok(eventService.getUserEventById(userId, eventId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<ResponseEventDto> patchUserEvent(@PathVariable long userId,
                                                           @PathVariable long eventId,
                                                           @RequestBody @Valid PatchEventDto dto) {

        return ResponseEntity.ok(eventService.changeEvent(userId, eventId, dto));
    }

    @GetMapping("/events")
    public ResponseEntity<Collection<ResponseEventDto>> findPublicEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        return ResponseEntity.ok(eventService.findEvents(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size, request));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<ResponseEventDto> getPublicEvent(@PathVariable long id,
                                                           HttpServletRequest request) {

        return ResponseEntity.ok(eventService.getPublicEvent(id, request));
    }

    @GetMapping("/admin/events")
    public ResponseEntity<Collection<ResponseEventDto>> findAdminEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        // Валидация дат
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Дата начала не может быть позже даты окончания");
        }

        // Дефолтные значения дат, если не указаны
        LocalDateTime startDate = rangeStart != null ? rangeStart : LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(1);
        return ResponseEntity.ok(eventService.findAdminEvents(users, states, categories,
                startDate, endDate, from, size));
    }

    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<ResponseEventDto> patchAdminEvent(@PathVariable long eventId,
                                                            @RequestBody @Valid PatchEventDto dto) {

        return ResponseEntity.ok(eventService.changeEventByAdmin(eventId, dto));
    }

    @PatchMapping("/admin/events/{eventId}/publish")
    public ResponseEntity<ResponseEventDto> publishEvent(@PathVariable long eventId) {
        PatchEventDto dto = new PatchEventDto();
        dto.setStateAction("PUBLISH_EVENT");
        return ResponseEntity.ok(eventService.changeEventByAdmin(eventId, dto));
    }

    @PatchMapping("/admin/events/{eventId}/cancel")
    public ResponseEntity<ResponseEventDto> cancelEvent(@PathVariable long eventId) {
        PatchEventDto dto = new PatchEventDto();
        dto.setStateAction("CANCEL_REVIEW");
        return ResponseEntity.ok(eventService.changeEventByAdmin(eventId, dto));
    }

    @PostMapping("/admin/events/{eventId}/reject")
    public ResponseEntity<ResponseEventDto> rejectEvent(@PathVariable long eventId) {
        PatchEventDto dto = new PatchEventDto();
        dto.setStateAction("REJECT_EVENT");
        return ResponseEntity.ok(eventService.changeEventByAdmin(eventId, dto));
    }
}
