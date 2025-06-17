package ru.practicum.explore.compilation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.MethodValidationResult;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.RequestCompilationDto;
import ru.practicum.explore.compilation.mapper.CompilationMapperNew;
import ru.practicum.explore.compilation.model.Compilation;
import ru.practicum.explore.compilation.model.Compilationevents;
import ru.practicum.explore.compilation.repository.CompilationRepository;
import ru.practicum.explore.compilation.repository.CompilationeventsRepository;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationeventsRepository compilationeventsRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto getCompilation(long compId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isPresent()) return CompilationMapperNew.mapToCompilationDto(compilation.get());
        else throw new EntityNotFoundException();
    }

    @Override
    public Collection<CompilationDto> getCompilations(String pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Collection<Compilation> compilations;
        if (!pinned.isEmpty()) {
            compilations = compilationRepository.findByPinned(Boolean.valueOf(pinned), page);
        } else {
            Page<Compilation> page1 = compilationRepository.findAll(page);
            compilations = page1.hasContent() ? page1.getContent() : Collections.emptyList();
        }
        return CompilationMapperNew.mapToCompilationDto(compilations);
    }

    @Override
    @Transactional
    public CompilationDto changeCompilation(long compId, RequestCompilationDto requestCompilationDto) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isPresent()) {
            if (requestCompilationDto.getEvents().size() != eventRepository.findAllById(requestCompilationDto.getEvents()).size())
                throw new EntityNotFoundException();
            Collection<Compilationevents> compilationevents = compilationeventsRepository.findByCompilationId(compId);
            compilationeventsRepository.deleteAll(compilationevents);
            compilationevents = new ArrayList<>();
            for (Long id : requestCompilationDto.getEvents()) {
                compilationevents.add(new Compilationevents(0L, compId, id));
            }
            compilationeventsRepository.saveAllAndFlush(compilationevents);
        }
        return CompilationMapperNew.mapToCompilationDto(compilationRepository.saveAndFlush(CompilationMapperNew.changeCompilation(compilation.get(), requestCompilationDto)));
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        compilationRepository.deleteById(compId);
        Collection<Compilationevents> compilationevents = compilationeventsRepository.findByCompilationId(compId);
        compilationeventsRepository.deleteAll(compilationevents);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(RequestCompilationDto requestCompilationDto) {
        List<Event> events = eventRepository.findAllById(requestCompilationDto.getEvents());
        if (requestCompilationDto.getEvents().size() != events.size() && !requestCompilationDto.getEvents().equals(List.of(0L)))
            throw new EntityNotFoundException();
        if (requestCompilationDto.getTitle().equals("null")) throw new MethodValidationException(MethodValidationResult.emptyResult());
        Compilation compilation = new Compilation();
        compilation = CompilationMapperNew.changeCompilation(compilation, requestCompilationDto);
        compilation.setEvents(events);
        compilation = compilationRepository.saveAndFlush(compilation);
        Collection<Compilationevents> compilationevents = new ArrayList<>();
        for (Long id : requestCompilationDto.getEvents()) {
            compilationevents.add(new Compilationevents(0L, compilation.getId(), id));
        }
        compilationeventsRepository.saveAllAndFlush(compilationevents);
        return CompilationMapperNew.mapToCompilationDto(compilation);
    }
}