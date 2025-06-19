package ru.practicum.explore.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.category.dto.*;
import ru.practicum.explore.category.mapper.CategoryMapper;
import ru.practicum.explore.category.service.CategoryService;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService service;
    private final CategoryMapper mapper;

    /* ---------- create ---------- */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtoWithId add(@RequestBody @Valid NewCategoryDto dto) {
        CategoryDto inner = mapper.toCategoryDto(dto);
        return service.createCategory(inner);
    }

    /* ---------- update ---------- */
    @PatchMapping("/{catId}")
    public CategoryDtoWithId update(@PathVariable @Positive long catId,
                                    @RequestBody @Valid NewCategoryDto dto) {
        CategoryDto inner = mapper.toCategoryDto(dto);
        return service.changeCategory(catId, inner);
    }

    /* ---------- delete ---------- */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long catId) {
        service.deleteCategory(catId);
    }

    /* ---------- list (постранично) ---------- */
    @GetMapping
    public Collection<CategoryDtoWithId> findAll(
            @RequestParam(defaultValue = "0") @Positive Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        return service.getAllCategories(from, size);
    }
}
