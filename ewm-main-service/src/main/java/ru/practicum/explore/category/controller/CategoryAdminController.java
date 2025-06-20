package ru.practicum.explore.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.service.CategoryService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtoWithId create(@RequestBody @Valid CategoryDto dto) {
        log.info("ADMIN create category {}", dto);
        return service.createCategory(dto);
    }

    @GetMapping
    public List<CategoryDtoWithId> findAll() {
        return List.copyOf(service.getAllCategories(0, Integer.MAX_VALUE));
    }

    @PatchMapping("/{catId}")
    public CategoryDtoWithId update(@PathVariable @Positive long catId,
                                    @RequestBody @Valid CategoryDto dto) {

        log.info("ADMIN update category id={}", catId);
        return service.changeCategory(catId, dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long catId) {
        log.info("ADMIN delete category {}", catId);
        service.deleteCategory(catId);
    }
}
