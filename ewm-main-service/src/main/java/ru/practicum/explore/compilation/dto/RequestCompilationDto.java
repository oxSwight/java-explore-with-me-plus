package ru.practicum.explore.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequestCompilationDto {

    @NotBlank
    @Size(max = 50)
    private String title;

    private Boolean pinned = false;

    private List<Long> events = new ArrayList<>();
}
