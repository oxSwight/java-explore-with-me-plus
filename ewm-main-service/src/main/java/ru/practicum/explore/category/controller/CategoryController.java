package ru.practicum.explore.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.service.CategoryService;
import ru.practicum.explore.global.service.ExchangeService;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories/{catId}")
    public ResponseEntity<CategoryDtoWithId> getCategory(@PathVariable long catId) {
        log.info("Request to get category with ID {} received.", catId);
        return ResponseEntity.ok().body(categoryService.getCategory(catId));
    }

    @GetMapping("/categories")
    public ResponseEntity<Collection<CategoryDtoWithId>> getAllCategories(@RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10") Integer size) {
        log.info("Request to get all categories received.");
        return ResponseEntity.ok().body(categoryService.getAllCategories(from, size));
    }

    @PatchMapping("/admin/categories/{catId}")
    public ResponseEntity<CategoryDtoWithId> changeCategory(@PathVariable long catId, @RequestBody CategoryDto categoryDto) {
        log.info("Request to change category {} received.", categoryDto);
        return ResponseEntity.ok().body(categoryService.changeCategory(catId, categoryDto));
    }

    @DeleteMapping("/admin/categories/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long catId) {
        log.info("Request to delete category with ID {} received.", catId);
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDtoWithId> createCategory(@RequestBody CategoryDto categoryDto) throws IOException {
        log.info("Request to create new category received: {}", categoryDto);
        CategoryDtoWithId categoryDtoWithId = categoryService.createCategory(categoryDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(categoryDtoWithId.getId()).toUri();
        log.info("New category created with ID {}", categoryDtoWithId.getId());
        return ResponseEntity.created(location).headers(ExchangeService.exchange(categoryDtoWithId)).body(categoryDtoWithId);
    }
}