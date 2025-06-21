package ru.practicum.explore.compilation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.common.exception.NotFoundException;
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

    private final CompilationRepository      compilationRepository;
    private final CompilationeventsRepository compilationeventsRepository;
    private final EventRepository             eventRepository;

    @Override
    public CompilationDto getCompilation(long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(EntityNotFoundException::new);
        return CompilationMapperNew.mapToCompilationDto(compilation);
    }

    @Override
    public Collection<CompilationDto> getCompilations(String pinned,
                                                      Integer from,
                                                      Integer size) {
        Boolean pin = pinned == null || pinned.isBlank() ? null : Boolean.valueOf(pinned);
        return getCompilations(pin, from, size);
    }

    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned,
                                                      Integer from,
                                                      Integer size) {

        int pageFrom = from == null ? 0  : from;
        int pageSize = size == null ? 10 : size;

        PageRequest page = PageRequest.of(pageFrom > 0 ? pageFrom / pageSize : 0,
                pageSize);

        Collection<Compilation> comps;
        if (pinned != null) {
            comps = compilationRepository.findByPinned(pinned, page);
        } else {
            Page<Compilation> p = compilationRepository.findAll(page);
            comps = p.hasContent() ? p.getContent() : Collections.emptyList();
        }
        return CompilationMapperNew.mapToCompilationDto(comps);
    }

    @Override
    @Transactional
    public CompilationDto changeCompilation(long compId,
                                            RequestCompilationDto dto) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(EntityNotFoundException::new);

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        /* Если поле events присутствует */
        if (dto.getEvents() != null) {

            /* Пустой список → не меняем существующие связи */
            if (!dto.getEvents().isEmpty()) {

                /* Текущее содержимое подборки */
                Set<Event> current = new HashSet<>(compilation.getEvents());

                for (Long eventId : dto.getEvents()) {

                    Event event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new NotFoundException("Event id=" + eventId + " not found"));

                    if (current.contains(event)) {
                        throw new DataIntegrityViolationException(
                                "Event id=" + eventId + " already in compilation");
                    }

                    /* Сохраняем связь в таблице compilation_events */
                    compilationeventsRepository.save(
                            new Compilationevents(0L, compilation.getId(), event.getId()));

                    current.add(event);
                }
                /* setEvents ожидает List, поэтому оборачиваем в ArrayList */
                compilation.setEvents(new ArrayList<>(current));
            }
        }

        Compilation saved = compilationRepository.saveAndFlush(compilation);
        return CompilationMapperNew.mapToCompilationDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(RequestCompilationDto dto) {

        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null && dto.getPinned());
        compilation = compilationRepository.saveAndFlush(compilation);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {

            List<Event> events = eventRepository.findAllById(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                throw new EntityNotFoundException();
            }

            for (Event e : events) {
                compilationeventsRepository.save(
                        new Compilationevents(0L, compilation.getId(), e.getId()));
            }
            compilation.setEvents(new ArrayList<>(events));
        }

        return CompilationMapperNew.mapToCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        compilationRepository.deleteById(compId);
        Collection<Compilationevents> links =
                compilationeventsRepository.findByCompilationId(compId);
        compilationeventsRepository.deleteAll(links);
    }
}
