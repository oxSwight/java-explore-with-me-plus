package ru.practicum.explore.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.explore.category.dto.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    /* NewCategoryDto → внутренний CategoryDto (используется сервисом) */
    CategoryDto toCategoryDto(NewCategoryDto src);

    /* CategoryDtoWithId → то же самое без изменений (удобно для контроллера) */
    CategoryDtoWithId toCategoryDtoWithId(CategoryDtoWithId src);
}
