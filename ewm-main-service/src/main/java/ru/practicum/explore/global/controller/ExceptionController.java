package ru.practicum.explore.global.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explore.common.dto.ApiError;
import ru.practicum.explore.common.exception.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice(basePackages = "ru.practicum")
public class ExceptionController {

    /* ---------- 404 ---------- */
    @ExceptionHandler({ NotFoundException.class, EntityNotFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(Exception ex) {
        return build(HttpStatus.NOT_FOUND, "Сущность не найдена", ex);
    }

    /* ---------- 400 ---------- */
    @ExceptionHandler({
            BadRequestException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            BindException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Некорректные данные", ex);
    }

    /* ---------- 403 ---------- */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, "Доступ запрещён", ex);
    }

    /* ---------- 409 ---------- */
    @ExceptionHandler({ ConflictException.class, DataIntegrityViolationException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(Exception ex) {
        return build(HttpStatus.CONFLICT, "Конфликт данных", ex);
    }

    /* ---------- 500 ---------- */
    @ExceptionHandler(Throwable.class)   // «catch-all» – последним!
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
