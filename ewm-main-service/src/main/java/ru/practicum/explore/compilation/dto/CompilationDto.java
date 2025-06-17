package ru.practicum.explore.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore.event.dto.ResponseEventDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<ResponseEventDto> events;
}