package ru.practicum.explore.event.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewEventDto {

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @Future
    private LocalDateTime eventDate;

    @Positive(message = "categoryId must be positive")
    private Long category;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean paid;

    private Boolean requestModeration;

    @NotNull
    private LocationDto location;
}
