package ru.practicum.explore.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.service.CategoryService;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /** GET / categories/{catId} */
    @GetMapping("/{catId}")
    public CategoryDtoWithId getCategory(@PathVariable @Positive long catId) {
        log.info("GET /categories/{}", catId);
        return categoryService.getCategory(catId);
    }

    /** GET / categories?from=0&size=10 */
    @GetMapping
    public Collection<CategoryDtoWithId> getAllCategories(
            @RequestParam(defaultValue = "0")  @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive       Integer size) {

        log.info("GET /categories  from={} size={}", from, size);
        return categoryService.getAllCategories(from, size);
    }
}
