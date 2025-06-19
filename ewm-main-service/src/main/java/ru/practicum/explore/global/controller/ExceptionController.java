package ru.practicum.explore.global.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explore.common.exception.*;
import ru.practicum.explore.common.dto.ApiError;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackages = "ru.practicum")   // охватываем все контроллеры
public class ExceptionController {

    /* ---------- 404 ---------- */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Сущность не найдена", ex);
    }

    /* ---------- 400 ---------- */
    @ExceptionHandler({
            BadRequestException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "Некорректный запрос", ex);
    }

    /* ---------- 403 ---------- */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, "Доступ запрещён", ex);
    }

    /* ---------- 409 ---------- */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "Конфликт данных", ex);
    }

    /* ---------- 500 ---------- */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Throwable ex) {
        log.error("Необработанная ошибка", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", ex);
    }

    /* ---------- helper ---------- */
    private ApiError build(HttpStatus status, String reason, Throwable ex) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(ex.getMessage())
                .errors(List.of())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
