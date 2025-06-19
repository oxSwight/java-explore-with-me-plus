package ru.practicum.explore.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApiError {
    private HttpStatus status;          // 400 / 404 / …
    private String reason;             // «Некорректный запрос»
    private String message;            // Текст исключения
    private List<String> errors;       // Подробности валидации
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;   // Время на сервере
}
