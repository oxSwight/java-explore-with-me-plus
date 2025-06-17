package ru.practicum.explore.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.RequestCompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable long compId) {
        log.info("Request to get compilation with ID {} received.", compId);
        return ResponseEntity.ok().body(compilationService.getCompilation(compId));
    }

    @GetMapping("/compilations")
    public ResponseEntity<Collection<CompilationDto>> getCompilations(@RequestParam String pinned, @RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10") Integer size) {
        log.info("Request to get compilations received.");
        return ResponseEntity.ok().body(compilationService.getCompilations(pinned, from, size));
    }

    @PatchMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> changeCompilation(@PathVariable long compId, @RequestBody RequestCompilationDto requestCompilationDto) {
        log.info("Request to change compilation with ID {} received.", compId);
        return ResponseEntity.ok().body(compilationService.changeCompilation(compId, requestCompilationDto));
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable long compId) {
        log.info("Request to delete compilation with ID {} received.", compId);
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody RequestCompilationDto requestCompilationDto) {
        log.info("Request to create new compilation received: {}", requestCompilationDto);
        CompilationDto compilationDto = compilationService.createCompilation(requestCompilationDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(compilationDto.getId()).toUri();
        log.info("New user created with ID {}", compilationDto.getId());
        return ResponseEntity.created(location).body(compilationDto);
    }
}