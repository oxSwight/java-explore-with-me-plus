package ru.practicum.explore.category.service;

import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryDtoWithId;

import java.util.Collection;

public interface CategoryService {
    CategoryDtoWithId getCategory(long catId);

    Collection<CategoryDtoWithId> getAllCategories(Integer from, Integer size);

    CategoryDtoWithId changeCategory(long catId, CategoryDto categoryDto);

    void deleteCategory(long catId);

    CategoryDtoWithId createCategory(CategoryDto categoryDto);
}