package ru.practicum.explore.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.RequestCompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import java.net.URI;
import java.util.Collection;

@Validated
@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService service;

    @GetMapping("/{compId}")
    public CompilationDto get(@PathVariable @Positive Long compId) {
        return service.getCompilation(compId);
    }

    @GetMapping
    public Collection<CompilationDto> getAll(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        return service.getCompilations((pinned), from, size);
    }

    @PostMapping("/admin")
    public ResponseEntity<CompilationDto> add(@RequestBody @Valid RequestCompilationDto dto) {
        CompilationDto saved = service.createCompilation(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping("/admin/{compId}")
    public CompilationDto update(@PathVariable @Positive Long compId,
                                 @RequestBody @Valid RequestCompilationDto dto) {
        return service.changeCompilation(compId, dto);
    }

    @DeleteMapping("/admin/{compId}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long compId) {
        service.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }
}
