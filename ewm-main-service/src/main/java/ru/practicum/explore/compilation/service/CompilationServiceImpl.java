package ru.practicum.explore.compilation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.common.exception.BadRequestException;
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

    private final CompilationRepository       compilationRepository;
    private final CompilationeventsRepository compilationeventsRepository;
    private final EventRepository             eventRepository;

    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        Collection<Compilation> comps = pinned != null
                ? compilationRepository.findByPinned(pinned, page)
                : compilationRepository.findAll(page).getContent();

        return CompilationMapperNew.mapToCompilationDto(comps);
    }


    @Override
    public CompilationDto getCompilation(long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(EntityNotFoundException::new);
        return CompilationMapperNew.mapToCompilationDto(comp);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(RequestCompilationDto dto) {

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Compilation title is required");
        }

        Compilation comp = new Compilation();
        comp.setTitle(dto.getTitle());
        comp.setPinned(dto.getPinned() != null && dto.getPinned());

        comp = compilationRepository.saveAndFlush(comp);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                throw new EntityNotFoundException();
            }
            for (Event e : events) {
                compilationeventsRepository.save(
                        new Compilationevents(0L, comp.getId(), e.getId()));
            }
            comp.setEvents(new ArrayList<>(events));
        } else {
            comp.setEvents(new ArrayList<>());
        }
        return CompilationMapperNew.mapToCompilationDto(comp);
    }

    @Override
    @Transactional
    public CompilationDto changeCompilation(long compId,
                                            RequestCompilationDto dto) {

        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(EntityNotFoundException::new);

        if (dto.getTitle() != null) {
            comp.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            comp.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            if (dto.getEvents().isEmpty()) {
                comp.setEvents(comp.getEvents() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(comp.getEvents()));
            } else {
                Set<Event> current = new HashSet<>(Optional.ofNullable(comp.getEvents())
                        .orElse(new ArrayList<>()));

                for (Long id : dto.getEvents()) {
                    Event e = eventRepository.findById(id)
                            .orElseThrow(() ->
                                    new NotFoundException("Event id=" + id + " not found"));
                    if (current.contains(e)) {
                        throw new DataIntegrityViolationException("Event already in compilation");
                    }
                    current.add(e);
                    compilationeventsRepository.save(
                            new Compilationevents(0L, comp.getId(), e.getId()));
                }
                comp.setEvents(new ArrayList<>(current));
            }
        }

        Compilation saved = compilationRepository.saveAndFlush(comp);
        return CompilationMapperNew.mapToCompilationDto(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        compilationRepository.deleteById(compId);
        compilationeventsRepository.deleteAll(
                compilationeventsRepository.findByCompilationId(compId));
    }
}
