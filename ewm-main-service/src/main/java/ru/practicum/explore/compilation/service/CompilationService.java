package ru.practicum.explore.compilation.service;

import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.RequestCompilationDto;

import java.util.Collection;

public interface CompilationService {
    CompilationDto getCompilation(long compId);

    Collection<CompilationDto> getCompilations(String pinned, Integer from, Integer size);

    CompilationDto changeCompilation(long compId, RequestCompilationDto requestCompilationDto);

    void deleteCompilation(long compId);

    CompilationDto createCompilation(RequestCompilationDto requestCompilationDto);
}