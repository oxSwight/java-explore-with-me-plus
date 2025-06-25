package ru.practicum.explore.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore.category.dto.*;

@Component
public class CategoryMapper {

    public CategoryDto toCategoryDto(NewCategoryDto src) {
        CategoryDto dto = new CategoryDto();
        dto.setName(src.getName());
        return dto;
    }
}