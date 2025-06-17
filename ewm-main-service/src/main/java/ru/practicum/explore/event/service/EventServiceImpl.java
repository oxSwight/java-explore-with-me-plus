package ru.practicum.explore.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.repository.CategoryRepository;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;
import ru.practicum.explore.event.mapper.EventMapperNew;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.model.Location;
import ru.practicum.explore.global.dto.SortValues;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.event.repository.LocationRepository;
import ru.practicum.explore.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Override
    public EventDto getEventById(long userId, long eventId) {
        Optional<Event> event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event.isPresent()) return EventMapperNew.mapToEventDto(event.get());
        else throw new EntityNotFoundException();
    }

    @Override
    public Collection<ResponseEventDto> getAllUserEvents(long userId, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return EventMapperNew.mapToResponseEventDto(eventRepository.findByInitiatorId(userId, page));
    }

    @Override
    @Transactional
    public EventDto changeEvent(long userId, long eventId, PatchEventDto patchEventDto) {
        Optional<Event> event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event.isPresent()) {
            if (event.get().getState().equals(Statuses.PUBLISHED.name()))
                throw new DataIntegrityViolationException("Data integrity violation exception");
            Optional<Category> category;
            Category cat = null;
            if (patchEventDto.getCategory() != null) {
                category = categoryRepository.findById(patchEventDto.getCategory());
                if (category.isPresent()) cat = category.get();
            }
            Location location = null;
            if (patchEventDto.getLocation() != null)
                location = locationRepository.saveAndFlush(EventMapperNew.mapToLocation(patchEventDto.getLocation()));
            String state = event.get().getState();
            Event newEvent = EventMapperNew.changeEvent(event.get(), patchEventDto);
            if (cat != null) newEvent.setCategory(cat);
            if (location != null) newEvent.setLocation(location);
            if (patchEventDto.getStateAction() != null) {
                switch (patchEventDto.getStateAction()) {
                    case "SEND_TO_REVIEW":
                        newEvent.setState(Statuses.PENDING.name());
                        break;
                    case "CANCEL_REVIEW":
                        newEvent.setState(Statuses.CANCELED.name());
                        break;
                    default:
                }
            } else newEvent.setState(state);
            return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(newEvent));
        } else throw new EntityNotFoundException();
    }

    @Override
    @Transactional
    public EventDto createEvent(long userId, PatchEventDto newEventDto) {
        Optional<Category> category = categoryRepository.findById(newEventDto.getCategory());
        Optional<User> user = userRepository.findById(userId);
        if (category.isPresent() && user.isPresent()) {
            Event event = new Event();
            event = EventMapperNew.changeEvent(event, newEventDto);
            Location location = locationRepository.saveAndFlush(EventMapperNew.mapToLocation(newEventDto.getLocation()));
            event.setLocation(location);
            event.setCategory(category.get());
            event.setInitiator(user.get());
            event.setViews(0L);
            return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(event));
        } else throw new EntityNotFoundException();
    }

    @Override
    @Transactional
    public EventDto getPublishedEventById(long eventId, Integer views) {
        Optional<Event> event = eventRepository.findByIdAndState(eventId, Statuses.PUBLISHED.name());
        if (event.isPresent()) {
            Event updated = event.get();
            updated.setViews(Long.valueOf(views));
            updated = eventRepository.saveAndFlush(updated);
            return EventMapperNew.mapToEventDto(updated);
        } else throw new EntityNotFoundException();
    }

    @Override
    public Collection<ResponseEventDto> findEventsByUser(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<ResponseEventDto> result = new ArrayList<>();
        if (!text.equals(" ") || !text.isBlank()) {
            if (!categories.equals(List.of(0L))) {
                if (onlyAvailable) {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, categories, page)));
                } else {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, categories, page)));
                }
            } else {
                if (onlyAvailable) {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, page)));

                } else {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), text, text, page)));
                }
            }
        } else {
            if (!categories.equals(List.of(0L))) {
                if (onlyAvailable) {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), categories, page)));
                } else {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), categories, page)));
                }
            } else {
                if (onlyAvailable) {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                } else {
                    if (paid)
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(paid, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                }
            }
        }
        if (sort.equals(SortValues.VIEWS.name())) result.sort(Comparator.comparing(ResponseEventDto::getViews).reversed());
        if (sort.equals(SortValues.EVENT_DATE.name())) result.sort(Comparator.comparing(ResponseEventDto::getEventDate).reversed());
        return result;
    }

    @Override
    @Transactional
    public EventDto changeEventByAdmin(long eventId, PatchEventDto patchEventDto) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isPresent()) {
            Optional<Category> category;
            Category cat = null;
            if (patchEventDto.getCategory() != null) {
                category = categoryRepository.findById(patchEventDto.getCategory());
                if (category.isPresent()) cat = category.get();
            }
            Location location = null;
            if (patchEventDto.getLocation() != null)
                location = locationRepository.saveAndFlush(EventMapperNew.mapToLocation(patchEventDto.getLocation()));
            String state = event.get().getState();
            Event newEvent = EventMapperNew.changeEvent(event.get(), patchEventDto);
            if (cat != null) newEvent.setCategory(cat);
            if (location != null) newEvent.setLocation(location);
            if (patchEventDto.getStateAction() != null) {
                switch (patchEventDto.getStateAction()) {
                    case "REJECT_EVENT": {
                        if (state.equals(Statuses.PUBLISHED.name()))
                            throw new DataIntegrityViolationException("Data integrity violation exception");
                        newEvent.setState(Statuses.CANCELED.name());
                        return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(newEvent));
                    }
                    case "PUBLISH_EVENT": {
                        if (state.equals(Statuses.PUBLISHED.name()) || state.equals(Statuses.CANCELED.name()))
                            throw new DataIntegrityViolationException("Data integrity violation exception");
                        newEvent.setState(Statuses.PUBLISHED.name());
                        newEvent.setPublishedOn(LocalDateTime.now());
                        return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(newEvent));
                    }
                    default:
                }
            } else newEvent.setState(state);
            return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(newEvent));
        } else throw new EntityNotFoundException();
    }

    @Override
    public Collection<EventDto> findEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<EventDto> result = new ArrayList<>();
        if (!users.equals(List.of(0L))) {
            if (!states.equals(List.of("0"))) {
                if (!categories.equals(List.of(0L))) {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(users, states, categories, rangeStart, rangeEnd, page)));
                } else {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByInitiatorIdInAndStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(users, states, rangeStart, rangeEnd, page)));
                }
            } else {
                if (!categories.equals(List.of(0L))) {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByInitiatorIdInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(users, categories, rangeStart, rangeEnd, page)));
                } else {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByInitiatorIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(users, rangeStart, rangeEnd, page)));
                }
            }
        } else {
            if (!states.equals(List.of("0"))) {
                if (!categories.equals(List.of(0L))) {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(states, categories, rangeStart, rangeEnd, page)));
                } else {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(states, rangeStart, rangeEnd, page)));
                }
            } else {
                if (!categories.equals(List.of(0L))) {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(categories, rangeStart, rangeEnd, page)));
                } else {
                    result.addAll(EventMapperNew.mapToEventDto(eventRepository.findByEventDateGreaterThanEqualAndEventDateLessThanEqual(rangeStart, rangeEnd, page)));
                }
            }
        }
        return result;
    }
}