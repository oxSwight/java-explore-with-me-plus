package ru.practicum.explore.category.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.mapper.CategoryMapperNew;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.repository.CategoryRepository;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDtoWithId getCategory(long catId) {
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isPresent()) return CategoryMapperNew.mapToCategoryDtoWithId(category.get());
        else throw new EntityNotFoundException();
    }

    @Override
    public Collection<CategoryDtoWithId> getAllCategories(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return CategoryMapperNew.mapToCategoryDtoWithId(categoryRepository.findAll(page));
    }

    @Override
    @Transactional
    public CategoryDtoWithId changeCategory(long catId, CategoryDto categoryDto) {
        Optional<Category> category = categoryRepository.findById(catId);
        Optional<Category> name = categoryRepository.findByName(categoryDto.getName());
        if (category.isPresent()) {
            if (name.isPresent() && name.get().getId() != (catId))
                throw new DataIntegrityViolationException("Data integrity violation exception");
            return CategoryMapperNew.mapToCategoryDtoWithId(categoryRepository.saveAndFlush(CategoryMapperNew.mapToCategory(category.get(), categoryDto)));
        } else throw new EntityNotFoundException();
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        Optional<Event> event = eventRepository.findByCategoryId(catId);
        if (event.isPresent()) throw new DataIntegrityViolationException("Data integrity violation exception");
        else categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDtoWithId createCategory(CategoryDto categoryDto) {
        Optional<Category> name = categoryRepository.findByName(categoryDto.getName());
        if (name.isPresent()) throw new DataIntegrityViolationException("Data integrity violation exception");
        return CategoryMapperNew.mapToCategoryDtoWithId(categoryRepository.saveAndFlush(CategoryMapperNew.mapToCategory(categoryDto)));
    }
}