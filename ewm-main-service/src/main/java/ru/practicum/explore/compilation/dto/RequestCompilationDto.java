package ru.practicum.explore.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestCompilationDto {
    private String title;
    private Boolean pinned;
    private List<Long> events;
}