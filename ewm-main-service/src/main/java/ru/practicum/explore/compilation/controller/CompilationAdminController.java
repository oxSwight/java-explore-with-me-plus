package ru.practicum.explore.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.RequestCompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid RequestCompilationDto dto) {
        log.info("ADMIN create compilation {}", dto);
        return service.createCompilation(dto);
    }

    @GetMapping
    public Collection<CompilationDto> findAll(
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        return service.getCompilations(null, from, size);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable @Positive Long compId,
                                 @RequestBody @Valid RequestCompilationDto dto) {
        return service.changeCompilation(compId, dto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long compId) {
        service.deleteCompilation(compId);
    }
}
