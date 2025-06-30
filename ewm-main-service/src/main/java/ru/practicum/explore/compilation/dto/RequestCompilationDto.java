package ru.practicum.explore.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RequestCompilationDto {

    @Size(max = 50)
    private String title;

    private Boolean pinned;

    private List<Long> events;
}