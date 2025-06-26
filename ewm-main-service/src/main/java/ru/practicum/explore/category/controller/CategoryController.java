package ru.practicum.explore.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.service.CategoryService;

import java.net.URI;
import java.util.Collection;

@Validated
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public Collection<CategoryDtoWithId> getAll(
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        return service.getAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDtoWithId getById(@PathVariable @Positive Long catId) {
        return service.getCategory(catId);
    }

    @PostMapping("/admin")
    public ResponseEntity<CategoryDtoWithId> add(@RequestBody @Valid CategoryDto dto) {
        CategoryDtoWithId saved = service.createCategory(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping("/admin/{catId}")
    public CategoryDtoWithId update(@PathVariable @Positive Long catId,
                                    @RequestBody @Valid CategoryDto dto) {
        return service.changeCategory(catId, dto);
    }

    @DeleteMapping("/admin/{catId}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long catId) {
        service.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }
}
