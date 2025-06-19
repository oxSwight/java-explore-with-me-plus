package ru.practicum.explore.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationController {

    private final CompilationService compilationService;

    /** GET / compilations?from=0&size=10&pinned=true */
    @GetMapping
    public Collection<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        log.info("GET /compilations  pinned={} from={} size={}", pinned, from, size);
        return compilationService.getCompilations(String.valueOf(pinned), from, size);
    }

    /** GET / compilations/{compId} */
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable @Positive long compId) {
        log.info("GET /compilations/{}", compId);
        return compilationService.getCompilation(compId);
    }
}
