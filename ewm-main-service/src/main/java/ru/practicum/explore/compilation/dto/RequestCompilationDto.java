package ru.practicum.explore.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RequestCompilationDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned = false;

    private Set<Long> events;
}
