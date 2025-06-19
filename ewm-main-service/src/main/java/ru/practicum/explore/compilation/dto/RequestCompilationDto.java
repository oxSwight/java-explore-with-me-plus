package ru.practicum.explore.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RequestCompilationDto {

    @Size(max = 50)
    private String title;

    private Boolean pinned;

    private Set<Long> events;
}
