package ru.practicum.explore.event.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.repository.CategoryRepository;
import ru.practicum.explore.common.exception.BadRequestException;
import ru.practicum.explore.common.exception.NotFoundException;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.event.mapper.EventMapperNew;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.model.EventState;
import ru.practicum.explore.event.model.Location;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.event.repository.LocationRepository;
import ru.practicum.explore.global.dto.SortValues;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    private static final Map<Long, Set<String>> VIEWS_IP_CACHE = new ConcurrentHashMap<>();

    @Override
    public EventDto getEventById(long userId, long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .map(EventMapperNew::mapToEventDto)
                .orElseThrow(() ->
                        new NotFoundException("Event id=" + eventId + " not found for user " + userId));
    }

    @Override
    public EventDto getPublishedEventById(long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, String.valueOf(EventState.PUBLISHED))
                .orElseThrow(() ->
                        new NotFoundException("Published event id=" + eventId + " not found"));
        return EventMapperNew.mapToEventDto(event);
    }

    @Override
    public Collection<ResponseEventDto> getAllUserEvents(long userId, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return EventMapperNew.mapToResponseEventDto(eventRepository.findByInitiatorId(userId, page));
    }

    @Override
    @Transactional
    public ResponseEventDto changeEvent(long userId,
                                        long eventId,
                                        PatchEventDto patch) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(EntityNotFoundException::new);

        if (Statuses.PUBLISHED.name().equals(event.getState()))
            throw new DataIntegrityViolationException("Cannot modify published event");

        if (patch.getEventDate() != null) {
            validateFutureDate(patch.getEventDate());
        }

        Category newCategory = null;
        if (patch.getCategory() != null) {
            newCategory = categoryRepository.findById(patch.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location newLocation = null;
        if (patch.getLocation() != null) {
            newLocation = locationRepository.saveAndFlush(
                    EventMapperNew.mapToLocation(patch.getLocation()));
        }

        String stateBefore = event.getState();
        Event updated = EventMapperNew.changeEvent(event, patch);

        if (newCategory != null) updated.setCategory(newCategory);
        if (newLocation != null) updated.setLocation(newLocation);

        if (patch.getStateAction() != null) {
            switch (patch.getStateAction()) {
                case "SEND_TO_REVIEW" -> updated.setState(Statuses.PENDING.name());
                case "CANCEL_REVIEW"  -> updated.setState(Statuses.CANCELED.name());
                default               -> { /* игнорируем */ }
            }
        } else {
            updated.setState(stateBefore);
        }

        updated = eventRepository.saveAndFlush(updated);
        return EventMapperNew.mapToResponseEventDto(updated);
    }

    @Override
    @Transactional
    public EventDto createEvent(long userId, PatchEventDto dto) {

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = new Event();
        EventMapperNew.changeEvent(event, dto);

        Location location = locationRepository.saveAndFlush(
                EventMapperNew.mapToLocation(dto.getLocation()));

        event.setLocation(location);
        event.setCategory(category);
        event.setInitiator(user);
        event.setViews(0L);

        return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(event));
    }

    @Override
    @Transactional
    public EventDto getPublishedEventById(long eventId, Integer views) {
        Event event = eventRepository.findByIdAndState(eventId, Statuses.PUBLISHED.name())
                .orElseThrow(EntityNotFoundException::new);

        event.setViews(Long.valueOf(views));
        event = eventRepository.saveAndFlush(event);
        return EventMapperNew.mapToEventDto(event);
    }

    @Override
    public Collection<ResponseEventDto> findEventsByUser(String text,
                                                         List<Long> categories,
                                                         Boolean paid,
                                                         LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd,
                                                         Boolean onlyAvailable,
                                                         String sort,
                                                         Integer from,
                                                         Integer size) {

        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<ResponseEventDto> result = new ArrayList<>();

        if (!text.equals(" ") || !text.isBlank()) {
            if (!categories.equals(List.of(0L))) {
                if (onlyAvailable) {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, categories, page)));
                } else {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, categories, page)));
                }
            } else { /* categories == [0] */
                if (onlyAvailable) {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, page)));
                } else {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                text, text, page)));
                }
            }
        } else {
            if (!categories.equals(List.of(0L))) {
                if (onlyAvailable) {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                categories, page)));
                } else {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                categories, page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(),
                                                categories, page)));
                }
            } else { /* categories == [0] */
                if (onlyAvailable) {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                } else {
                    if (Boolean.TRUE.equals(paid))
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(
                                                true, rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                    else
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository.
                                        findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(
                                                rangeStart, rangeEnd, Statuses.PUBLISHED.name(), page)));
                }
            }
        }

        /* сортировка */
        if (SortValues.VIEWS.name().equals(sort))
            result.sort(Comparator.comparing(ResponseEventDto::getViews).reversed());
        if (SortValues.EVENT_DATE.name().equals(sort))
            result.sort(Comparator.comparing(ResponseEventDto::getEventDate).reversed());

        return result;
    }

    @Override
    @Transactional
    public ResponseEventDto changeEventByAdmin(long eventId,
                                               PatchEventDto patch) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(EntityNotFoundException::new);

        Category newCategory = null;
        if (patch.getCategory() != null) {
            newCategory = categoryRepository.findById(patch.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location newLocation = null;
        if (patch.getLocation() != null) {
            newLocation = locationRepository.saveAndFlush(
                    EventMapperNew.mapToLocation(patch.getLocation()));
        }

        String currentState = event.getState();
        Event updated = EventMapperNew.changeEvent(event, patch);

        if (newCategory != null) updated.setCategory(newCategory);
        if (newLocation != null) updated.setLocation(newLocation);

        if (patch.getStateAction() != null) {
            switch (patch.getStateAction()) {
                case "REJECT_EVENT" -> {
                    if (Statuses.PUBLISHED.name().equals(currentState))
                        throw new DataIntegrityViolationException("Event already published");
                    updated.setState(Statuses.CANCELED.name());
                }
                case "PUBLISH_EVENT" -> {
                    if (Statuses.PUBLISHED.name().equals(currentState) ||
                            Statuses.CANCELED.name().equals(currentState))
                        throw new DataIntegrityViolationException("Event cannot be published");
                    updated.setState(Statuses.PUBLISHED.name());
                    updated.setPublishedOn(LocalDateTime.now());
                }
                default -> { /* игнор */ }
            }
        } else {
            updated.setState(currentState);
        }

        updated = eventRepository.saveAndFlush(updated);
        return EventMapperNew.mapToResponseEventDto(updated);
    }

    @Override
    public Collection<ResponseEventDto> findEventsByAdmin(List<Long> users,
                                                          List<String> states,
                                                          List<Long> categories,
                                                          LocalDateTime rangeStart,
                                                          LocalDateTime rangeEnd,
                                                          Integer from,
                                                          Integer size) {

        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<ResponseEventDto> result = new ArrayList<>();

        if (!users.equals(List.of(0L))) {
            if (!states.equals(List.of("0"))) {
                if (!categories.equals(List.of(0L)))
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            users, states, categories, rangeStart, rangeEnd, page)));
                else
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByInitiatorIdInAndStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            users, states, rangeStart, rangeEnd, page)));
            } else { /* states == ["0"] */
                if (!categories.equals(List.of(0L)))
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByInitiatorIdInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            users, categories, rangeStart, rangeEnd, page)));
                else
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByInitiatorIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            users, rangeStart, rangeEnd, page)));
            }
        } else { /* users == [0] */
            if (!states.equals(List.of("0"))) {
                if (!categories.equals(List.of(0L)))
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            states, categories, rangeStart, rangeEnd, page)));
                else
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            states, rangeStart, rangeEnd, page)));
            } else { /* states == ["0"] */
                if (!categories.equals(List.of(0L)))
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            categories, rangeStart, rangeEnd, page)));
                else
                    result.addAll(EventMapperNew.mapToResponseEventDto(
                            eventRepository.
                                    findByEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            rangeStart, rangeEnd, page)));
            }
        }
        return result;
    }

    @Override
    public Collection<ResponseEventDto> getUserEvents(long userId, int from, int size) {
        return getAllUserEvents(userId, from, size);
    }

    @Override
    public ResponseEventDto getUserEventById(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Event id=" + eventId + " not found for user " + userId));
        return EventMapperNew.mapToResponseEventDto(event);
    }

    @Override
    public Collection<ResponseEventDto> findEvents(String text,
                                                   List<Long> categories,
                                                   Boolean paid,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   Boolean onlyAvailable,
                                                   String sort,
                                                   Integer from,
                                                   Integer size,
                                                   HttpServletRequest request) {

        /* валидация диапазона дат */
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart))
            throw new BadRequestException("rangeEnd must be after rangeStart");

        from = from == null ? 0 : from;
        size = size == null ? 10 : size;
        sort = sort == null ? SortValues.EVENT_DATE.name() : sort;

        log.debug("PUBLIC SEARCH, ip={}, uri={}", request.getRemoteAddr(), request.getRequestURI());
        return findEventsByUser(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    @Override
    @Transactional
    public ResponseEventDto getPublicEvent(long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, Statuses.PUBLISHED.name())
                .orElseThrow(() ->
                        new NotFoundException("Published event id=" + eventId + " not found"));

        String ip = request.getRemoteAddr();
        VIEWS_IP_CACHE.computeIfAbsent(eventId, k -> ConcurrentHashMap.newKeySet());

        if (VIEWS_IP_CACHE.get(eventId).add(ip)) {     // true → первый раз с этого IP
            event.setViews(event.getViews() + 1);
            eventRepository.saveAndFlush(event);
        }
        return EventMapperNew.mapToResponseEventDto(event);
    }

    @Override
    public Collection<ResponseEventDto> findAdminEvents(List<Long> users,
                                                        List<String> states,
                                                        List<Long> categories,
                                                        LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd,
                                                        Integer from,
                                                        Integer size) {

        return findEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @Override
    @Transactional
    public ResponseEventDto createEvent(long userId, NewEventDto dto) {

        /* дата события должна быть как минимум +2 ч от now */
        validateFutureDate(dto.getEventDate());

        PatchEventDto patch = new PatchEventDto(
                dto.getAnnotation(), dto.getCategory(), dto.getDescription(),
                dto.getEventDate(), dto.getLocation(), dto.getPaid(),
                dto.getParticipantLimit(), dto.getRequestModeration(),
                null, dto.getTitle());

        EventDto created = createEvent(userId, patch);     // старый метод
        Event entity = eventRepository.findById(created.getId())
                .orElseThrow(() ->
                        new NotFoundException("Event not found after creation"));

        return EventMapperNew.mapToResponseEventDto(entity);
    }

    private void validateFutureDate(LocalDateTime target) {
        if (target == null) return;
        LocalDateTime limit = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.SECONDS);
        if (!target.isAfter(limit))
            throw new BadRequestException("eventDate must be at least 2 hours in the future");
    }
}
