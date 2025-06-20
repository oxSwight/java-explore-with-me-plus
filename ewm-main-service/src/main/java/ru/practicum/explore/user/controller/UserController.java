package ru.practicum.explore.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.global.service.ExchangeService;
import ru.practicum.explore.user.dto.*;
import ru.practicum.explore.user.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<RequestDto> createRequest(@PathVariable @Positive long userId,
                                                    @RequestParam @Positive Long eventId) {

        log.info("POST /users/{}/requests?eventId={} – create participation request", userId, eventId);
        RequestDto dto = userService.createRequest(userId, eventId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(@PathVariable @Positive long userId,
                                                    @PathVariable @Positive long requestId) {

        log.info("PATCH cancel request {} by user {}", requestId, userId);
        return ResponseEntity.ok(userService.cancelRequest(userId, requestId));
    }

    /** Получить все заявки текущего пользователя. GET /users/{userId}/requests */
    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<Collection<RequestDto>> getUserRequests(@PathVariable @Positive long userId) {

        log.info("GET all requests of user {}", userId);
        return ResponseEntity.ok(userService.getUserRequests(userId));
    }

    /** Получить заявки на событие, созданное текущим пользователем-инициатором. */
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<Collection<RequestDto>> getEventRequests(@PathVariable @Positive long userId,
                                                                   @PathVariable @Positive long eventId) {

        log.info("GET requests for event {} by initiator {}", eventId, userId);
        return ResponseEntity.ok(userService.getEventRequests(userId, eventId));
    }

    /** Подтвердить/отклонить несколько заявок на участие. */
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<ResponseInformationAboutRequests> changeRequestsStatuses(
            @PathVariable @Positive long userId,
            @PathVariable @Positive long eventId,
            @RequestBody @Valid ChangedStatusOfRequestsDto dto) {

        log.info("PATCH change status of requests for event {} by user {}", eventId, userId);
        return ResponseEntity.ok(userService.changeRequestsStatuses(userId, eventId, dto));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<Collection<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /admin/users – ids={}, from={}, size={}", ids, from, size);
        return ResponseEntity.ok(userService.getAllUsers(ids, from, size));
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto) throws IOException {

        log.info("POST /admin/users – body={}", userDto);
        UserDto saved = userService.createUser(userDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        /* ExchangeService формирует, например, ETag/Last-Modified для кэширования. */
        return ResponseEntity.created(location)
                .headers(ExchangeService.exchange(saved))
                .body(saved);
    }

    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive long userId) {

        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
