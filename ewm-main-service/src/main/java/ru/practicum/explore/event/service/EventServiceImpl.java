package ru.practicum.explore.event.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.repository.CategoryRepository;
import ru.practicum.explore.common.exception.BadRequestException;
import ru.practicum.explore.common.exception.ConflictException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final UserRepository     userRepository;
    private final EventRepository    eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    private static final Map<Long, Set<String>> VIEWS_IP_CACHE = new ConcurrentHashMap<>();

    @Override
    public EventDto getEventById(long userId, long eventId) {
        return eventRepository
                .findByIdAndInitiatorId(eventId, userId)
                .map(EventMapperNew::mapToEventDto)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Event id=" + eventId + " not found for user " + userId));
    }

    @Override
    public EventDto getPublishedEventById(long eventId) {
        Event event = eventRepository
                .findByIdAndState(eventId, EventState.PUBLISHED.name())
                .orElseThrow(() ->
                        new NotFoundException("Published event id=" + eventId + " not found"));
        return EventMapperNew.mapToEventDto(event);
    }

    @Override
    public Collection<ResponseEventDto> getAllUserEvents(long userId,
                                                         Integer from,
                                                         Integer size) {

        //int pageFrom = from == null ? 0 : from;
        //int pageSize = size == null ? 10 : size; // Не заменяем size=0!
        //int pageSize = (size == null || size <= 0) ? 10 : size;
        //PageRequest page = PageRequest.of(pageFrom > 0 ? pageFrom / pageSize : 0, pageSize);
        //PageRequest page = PageRequest.of(pageFrom, pageSize); // Простая пагинация
        //return EventMapperNew.mapToResponseEventDto(eventRepository.findByInitiatorId(userId, page));
        PageRequest pageRequest = createPageRequest(from, size);
        Page<Event> eventsPage = eventRepository.findByInitiatorId(userId, pageRequest);

        return eventsPage.getContent()
                .stream()
                .map(EventMapperNew::mapToResponseEventDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseEventDto changeEvent(long userId,
                                        long eventId,
                                        PatchEventDto patch) {

        Event stored = eventRepository
                .findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(EntityNotFoundException::new);

        if (Statuses.PUBLISHED.name().equals(stored.getState())) {
            throw new ConflictException("Cannot modify published event");
        }

        if (patch.getEventDate() != null) {
            validateFutureDate(patch.getEventDate());
        }

        Category category = null;
        if (patch.getCategory() != null) {
            category = categoryRepository
                    .findById(patch.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location location = null;
        if (patch.getLocation() != null) {
            location = locationRepository
                    .saveAndFlush(EventMapperNew.mapToLocation(patch.getLocation()));
        }

        Event updated = EventMapperNew.changeEvent(stored, patch);

        if (category != null) {
            updated.setCategory(category);
        }
        if (location != null) {
            updated.setLocation(location);
        }

        applyStateAction(patch.getStateAction(), stored.getState(), updated);

        return EventMapperNew
                .mapToResponseEventDto(eventRepository.saveAndFlush(updated));
    }

    @Override
    @Transactional
    public EventDto createEvent(long userId, PatchEventDto dto) {

        Category category = categoryRepository
                .findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = new Event();
        EventMapperNew.changeEvent(event, dto);

        Location location = locationRepository
                .saveAndFlush(EventMapperNew.mapToLocation(dto.getLocation()));

        event.setLocation(location);
        event.setCategory(category);
        event.setInitiator(user);
        event.setViews(0L);

        return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(event));
    }

    @Override
    @Transactional
    public EventDto getPublishedEventById(long eventId, Integer views) {
        Event event = eventRepository
                .findByIdAndState(eventId, Statuses.PUBLISHED.name())
                .orElseThrow(EntityNotFoundException::new);

        event.setViews(Long.valueOf(views));
        return EventMapperNew.mapToEventDto(eventRepository.saveAndFlush(event));
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

        String  qText      = text == null ? "" : text.trim();
        boolean byText     = !qText.isBlank();
        boolean byCats     = categories != null && !categories.isEmpty();
        List<Long> cats    = byCats ? categories : List.of();
        boolean isPaid     = Boolean.TRUE.equals(paid);
        boolean onlyAvail  = Boolean.TRUE.equals(onlyAvailable);

        /* пагинация */
        int pageFrom = from == null ? 0 : from;
        int pageSize = (size == null || size <= 0) ? 10 : size;
        PageRequest page = PageRequest.of(pageFrom / pageSize, pageSize);

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end   = rangeEnd   != null ? rangeEnd   : start.plusYears(100);

        List<ResponseEventDto> result = new ArrayList<>();

        if (byText) {
            if (!cats.isEmpty()) {
                /* text + categories */
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    }
                }

            } else { /* text без категорий */
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    }
                }
            }

        } else {

            if (!cats.isEmpty()) {   /* категории без текста */
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    }
                }

            } else { /* ни текста, ни категорий */
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    }
                }
            }
        }

        /* ---------- сортировка ---------- */
        if (sort != null && SortValues.VIEWS.name().equals(sort)) {
            result.sort(Comparator.comparing(ResponseEventDto::getViews).reversed());
        } else {
            result.sort(Comparator.comparing(ResponseEventDto::getEventDate).reversed());
        }
        return result;
    }

    @Override
    @Transactional
    public ResponseEventDto changeEventByAdmin(long eventId, PatchEventDto patch) {

        Event stored = eventRepository.findById(eventId)
                .orElseThrow(EntityNotFoundException::new);

        if (patch.getEventDate() != null) {
            validateFutureDate(patch.getEventDate());
        }

        Category category = null;
        if (patch.getCategory() != null) {
            category = categoryRepository
                    .findById(patch.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location location = null;
        if (patch.getLocation() != null) {
            location = locationRepository
                    .saveAndFlush(EventMapperNew.mapToLocation(patch.getLocation()));
        }

        Event updated = EventMapperNew.changeEvent(stored, patch);

        if (category != null) {
            updated.setCategory(category);
        }
        if (location != null) {
            updated.setLocation(location);
        }

        applyStateAction(patch.getStateAction(), stored.getState(), updated);

        return EventMapperNew
                .mapToResponseEventDto(eventRepository.saveAndFlush(updated));
    }

    @Transactional
    public ResponseEventDto publishEventByAdmin(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(EntityNotFoundException::new);

        if (Statuses.PUBLISHED.name().equals(event.getState())
                || Statuses.CANCELED.name().equals(event.getState())) {
            throw new ConflictException("Event cannot be published");
        }

        event.setState(Statuses.PUBLISHED.name());
        event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return EventMapperNew.mapToResponseEventDto(eventRepository.saveAndFlush(event));
    }

    @Transactional
    public ResponseEventDto cancelEventByAdmin(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(EntityNotFoundException::new);

        if (Statuses.PUBLISHED.name().equals(event.getState())) {
            throw new ConflictException("Published event cannot be cancelled");
        }

        event.setState(Statuses.CANCELED.name());
        return EventMapperNew.mapToResponseEventDto(eventRepository.saveAndFlush(event));
    }

    @Override
    public Collection<ResponseEventDto> findEventsByAdmin(List<Long> users,
                                                          List<String> states,
                                                          List<Long> categories,
                                                          LocalDateTime rangeStart,
                                                          LocalDateTime rangeEnd,
                                                          Integer from,
                                                          Integer size) {

        List<Long>   ids   = users      == null || users.isEmpty()      ? List.of(0L) : users;
        List<String> st    = states     == null || states.isEmpty()     ? List.of("0") : states;
        List<Long>   cats  = categories == null || categories.isEmpty() ? List.of(0L) : categories;

        int pageFrom = from == null ? 0 : from;
        int pageSize = (size == null || size <= 0) ? 10 : size;
        PageRequest page = PageRequest
                .of(pageFrom > 0 ? pageFrom / pageSize : 0, pageSize);

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.MIN;
        LocalDateTime end   = rangeEnd   != null ? rangeEnd   : LocalDateTime.MAX;

        List<ResponseEventDto> result = new ArrayList<>();

        /* логика выборки без изменений, только start/end вместо raw-параметров */
        if (!ids.equals(List.of(0L))) {
            if (!st.equals(List.of("0"))) {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            ids, st, cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByInitiatorIdInAndStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            ids, st, start, end, page)));
                }
            } else {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByInitiatorIdInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            ids, cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByInitiatorIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            ids, start, end, page)));
                }
            }
        } else {
            if (!st.equals(List.of("0"))) {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            st, cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            st, start, end, page)));
                }
            } else {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByEventDateGreaterThanEqualAndEventDateLessThanEqual(
                                            start, end, page)));
                }
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
        Event event = eventRepository
                .findByIdAndInitiatorId(eventId, userId)
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

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }
        return findEventsByUser(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    @Override
    @Transactional
    public ResponseEventDto getPublicEvent(long eventId, HttpServletRequest request) {
        Event event = eventRepository
                .findByIdAndState(eventId, Statuses.PUBLISHED.name())
                .orElseThrow(() ->
                        new NotFoundException("Published event id=" + eventId + " not found"));

        String ip = request.getRemoteAddr();
        VIEWS_IP_CACHE.computeIfAbsent(eventId, k -> ConcurrentHashMap.newKeySet());

        if (VIEWS_IP_CACHE.get(eventId).add(ip)) {
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

        validateFutureDate(dto.getEventDate());

        PatchEventDto patch = new PatchEventDto(
                dto.getAnnotation(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getEventDate(),
                dto.getLocation(),
                dto.getPaid(),
                dto.getParticipantLimit(),
                dto.getRequestModeration(),
                null,
                dto.getTitle());

        EventDto created = createEvent(userId, patch);
        Event   entity  = eventRepository
                .findById(created.getId())
                .orElseThrow(() ->
                        new NotFoundException("Event not found after creation"));

        return EventMapperNew.mapToResponseEventDto(entity);
    }

    private void validateFutureDate(LocalDateTime target) {
        if (target == null) {
            return;
        }
        LocalDateTime limit = LocalDateTime.now()
                .plusHours(2)
                .truncatedTo(ChronoUnit.SECONDS);
        if (!target.isAfter(limit)) {
            throw new BadRequestException("eventDate must be at least 2 hours in the future");
        }
    }

    private void applyStateAction(String stateAction, String prevState, Event updated) {
        if (stateAction == null) {
            updated.setState(prevState);
            return;
        }
        switch (stateAction) {
            case "SEND_TO_REVIEW" -> updated.setState(Statuses.PENDING.name());

            case "CANCEL_REVIEW", "REJECT_EVENT" -> {
                if (Statuses.PUBLISHED.name().equals(prevState)) {
                    throw new ConflictException("Event already published");
                }
                updated.setState(Statuses.CANCELED.name());
            }

            case "PUBLISH_EVENT" -> {
                if (Statuses.PUBLISHED.name().equals(prevState)
                        || Statuses.CANCELED.name().equals(prevState)) {
                    throw new ConflictException("Event cannot be published");
                }
                updated.setState(Statuses.PUBLISHED.name());
                updated.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            }

            default -> updated.setState(prevState);
        }
    }

    private PageRequest createPageRequest(Integer from, Integer size) {
        int pageNumber = from == null ? 0 : Math.max(from, 0);
        int pageSize = size == null ? 10 : Math.max(size, 0);
        return PageRequest.of(pageNumber, pageSize);
    }
}
