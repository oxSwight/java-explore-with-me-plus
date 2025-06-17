package ru.practicum.explore.global.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explore.global.dto.ErrorMessage;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFound(final EntityNotFoundException e) {
        log.warn("Encountered {}: returning 404 Error. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(List.of(e.getClass().getSimpleName(), "Entity not found in DB"), e.getLocalizedMessage(), "Entity not found in DB");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(errorMessage);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorMessage> handleBadRequest(final BadRequestException e) {
        log.warn("Encountered {}: returning 400 Error. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(List.of(e.getClass().getSimpleName(), "Error in request"), e.getLocalizedMessage(), "Error in request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errorMessage);
    }

    @ExceptionHandler(MethodValidationException.class)
    public ResponseEntity<ErrorMessage> handleMethodValidationException(final MethodValidationException e) {
        log.warn("Encountered {}: returning 400 Error. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(List.of(e.getClass().getSimpleName(), "Error in request"), e.getLocalizedMessage(), "Error in request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessage> handleInternalServerError(final RuntimeException e) {
        log.warn("Encountered {}: returning 500 Error. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(List.of(e.getClass().getSimpleName(), "Error in request"), e.getLocalizedMessage(), "Error in request");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolationException(final ConstraintViolationException e) {
        log.warn("Encountered {}: returning 400 Error. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(List.of(e.getClass().getSimpleName(), "Error in request"), e.getLocalizedMessage(), "Error in request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errorMessage);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleBadEntity(final DataIntegrityViolationException e) {
        log.warn("Encountered {}: returning 409 Error. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(List.of(e.getClass().getSimpleName(), "Conflict: Data integrity violation exception"), e.getLocalizedMessage(), "Conflict: Data integrity violation exception");
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(errorMessage);
    }
}