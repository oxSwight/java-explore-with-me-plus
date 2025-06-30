package ru.practicum.explore.global.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explore.common.exception.BadRequestException;
import ru.practicum.explore.common.exception.ConflictException;
import ru.practicum.explore.common.exception.NotFoundException;
import ru.practicum.explore.global.dto.ErrorMessage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadRequestException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorMessage> handleBadRequest(Exception ex) {
        List<String> errors = extractErrors(ex);
        log.debug("400 BAD_REQUEST: {}", errors);
        return build(HttpStatus.BAD_REQUEST,
                "Validation failed", ex.getMessage(), errors);
    }

    @ExceptionHandler({NotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorMessage> handleNotFound(Exception ex) {
        log.debug("404 NOT_FOUND: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND,
                "Entity not found", ex.getMessage(), List.of());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, IllegalStateException.class, ConflictException.class})
    public ResponseEntity<ErrorMessage> handleConflict(Exception ex) {
        log.debug("409 CONFLICT: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT,
                "Conflict", ex.getMessage(), List.of());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorMessage> handleAll(Throwable ex) {
        log.error("500 INTERNAL_SERVER_ERROR", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error", ex.getMessage(), List.of());
    }

    private ResponseEntity<ErrorMessage> build(HttpStatus status,
                                               String reason,
                                               String message,
                                               List<String> errors) {
        return ResponseEntity.status(status)
                .body(ErrorMessage.of(status, reason, message, errors));
    }

    private List<String> extractErrors(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException m) {
            return m.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
        }
        if (ex instanceof ConstraintViolationException c) {
            return c.getConstraintViolations()
                    .stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.toList());
        }
        return List.of(ex.getMessage());
    }
}
