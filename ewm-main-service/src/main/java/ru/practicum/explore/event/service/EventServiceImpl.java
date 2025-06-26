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
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.common.exception.BadRequestException;
import ru.practicum.explore.common.exception.ConflictException;
import ru.practicum.explore.common.exception.NotFoundException;
import ru.practicum.explore.dto.StatDto;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.NewEventDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;
import ru.practicum.explore.event.mapper.EventMapperNew;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.model.Location;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.event.repository.LocationRepository;
import ru.practicum.explore.global.dto.SortValues;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
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
    @Transactional
    public EventDto getPublishedEventById(long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, Statuses.PUBLISHED.name())
                .orElseThrow(EntityNotFoundException::new);

        long views = fetchViews("/events/" + eventId, false);
        event.setViews(views);
        eventRepository.save(event);

        return EventMapperNew.mapToEventDto(event);
    }

    @Override
    public Collection<ResponseEventDto> getAllUserEvents(long userId,
                                                         Integer from,
                                                         Integer size) {
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

        String qText = text == null ? "" : text.trim();
        boolean byText = !qText.isBlank();
        boolean byCats = categories != null && !categories.isEmpty();
        List<Long> cats = byCats ? categories : List.of();
        boolean isPaid = Boolean.TRUE.equals(paid);
        boolean onlyAvail = Boolean.TRUE.equals(onlyAvailable);

        int pageFrom = from == null ? 0 : from;
        int pageSize = (size == null || size <= 0) ? 10 : size;
        PageRequest page = PageRequest.of(pageFrom / pageSize, pageSize);

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end = rangeEnd != null ? rangeEnd : start.plusYears(100);

        List<ResponseEventDto> result = new ArrayList<>();

        if (byText) {
            if (!cats.isEmpty()) {
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidWithLimitStateTextAndCategory(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findLimitStateTextAndCategory(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidStateTextAndCategory(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findStateTextAndCategory(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, cats, page)));
                    }
                }

            } else {
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidWithLimitStateText(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findLimitStateText(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidStateText(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findStateText(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                qText, qText, page)));
                    }
                }
            }

        } else {

            if (!cats.isEmpty()) {
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidWithLimitStateCategory(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findLimitStateCategory(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidStateCategory(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findStateCategory(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                cats, page)));
                    }
                }

            } else {
                if (onlyAvail) {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidWithLimitState(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findLimitState(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    }
                } else {
                    if (isPaid) {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findPaidState(
                                                true, start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    } else {
                        result.addAll(EventMapperNew.mapToResponseEventDto(
                                eventRepository
                                        .findState(
                                                start, end,
                                                Statuses.PUBLISHED.name(),
                                                page)));
                    }
                }
            }
        }

        for (ResponseEventDto dto : result) {
            dto.setViews(fetchViews("/events/" + dto.getId(), false));
        }

        if (SortValues.VIEWS.name().equals(sort)) {
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
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (patch.getEventDate() != null) {
            validateFutureDate(patch.getEventDate());
            stored.setEventDate(patch.getEventDate());
        }

        if (patch.getCategory() != null) {
            Category category = categoryRepository.findById(patch.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            stored.setCategory(category);
        }

        if (patch.getLocation() != null) {
            Location location = locationRepository.save(
                    EventMapperNew.mapToLocation(patch.getLocation()));
            stored.setLocation(location);
        }

        if (patch.getAnnotation() != null) {
            stored.setAnnotation(patch.getAnnotation());
        }
        if (patch.getDescription() != null) {
            stored.setDescription(patch.getDescription());
        }
        if (patch.getPaid() != null) {
            stored.setPaid(patch.getPaid());
        }
        if (patch.getParticipantLimit() != null) {
            stored.setParticipantLimit(patch.getParticipantLimit());
        }
        if (patch.getRequestModeration() != null) {
            stored.setRequestModeration(patch.getRequestModeration());
        }
        if (patch.getTitle() != null) {
            stored.setTitle(patch.getTitle());
        }

        applyStateAction(patch.getStateAction(), stored.getState(), stored);

        Event updatedEvent = eventRepository.save(stored);
        return EventMapperNew.mapToResponseEventDto(updatedEvent);
    }

    @Override
    public Collection<ResponseEventDto> findEventsByAdmin(List<Long> users,
                                                          List<String> states,
                                                          List<Long> categories,
                                                          LocalDateTime rangeStart,
                                                          LocalDateTime rangeEnd,
                                                          Integer from,
                                                          Integer size) {

        List<Long> ids = users == null || users.isEmpty() ? List.of(0L) : users;
        List<String> st = states == null || states.isEmpty() ? List.of("0") : states;
        List<Long> cats = categories == null || categories.isEmpty() ? List.of(0L) : categories;

        int pageFrom = from == null ? 0 : from;
        int pageSize = (size == null || size <= 0) ? 10 : size;
        PageRequest page = PageRequest
                .of(pageFrom > 0 ? pageFrom / pageSize : 0, pageSize);

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now().minusYears(1);
        LocalDateTime end = rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(1);

        if (start.isAfter(end)) {
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        List<ResponseEventDto> result = new ArrayList<>();

        if (!ids.equals(List.of(0L))) {
            if (!st.equals(List.of("0"))) {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findUsersStatesCategories(
                                            ids, st, cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findUsersStates(
                                            ids, st, start, end, page)));
                }
            } else {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findUsersCategories(
                                            ids, cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findUsersEvents(
                                            ids, start, end, page)));
                }
            }
        } else {
            if (!st.equals(List.of("0"))) {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findStatesCategories(
                                            st, cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findStates(
                                            st, start, end, page)));
                }
            } else {
                if (!cats.equals(List.of(0L))) {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findCategories(
                                            cats, start, end, page)));
                } else {
                    result.addAll(EventMapperNew
                            .mapToResponseEventDto(eventRepository
                                    .findByDateRange(
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
                .orElseThrow(() -> new NotFoundException("Published event not found"));

        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .orElse(request.getRemoteAddr());

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
        Event entity = eventRepository
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
            return;
        }

        switch (stateAction) {
            case "PUBLISH_EVENT" -> {
                if (Statuses.PUBLISHED.name().equals(prevState)) {
                    throw new ConflictException("Event already published");
                }
                if (Statuses.CANCELED.name().equals(prevState)) {
                    throw new ConflictException("Cannot publish canceled event");
                }
                updated.setState(Statuses.PUBLISHED.name());
                updated.setPublishedOn(LocalDateTime.now());
            }
            case "CANCEL_REVIEW", "REJECT_EVENT" -> {
                if (Statuses.PUBLISHED.name().equals(prevState)) {
                    throw new ConflictException("Cannot cancel published event");
                }
                updated.setState(Statuses.CANCELED.name());
            }
            case "SEND_TO_REVIEW" -> updated.setState(Statuses.PENDING.name());
            default -> log.warn("Unknown state action: {}", stateAction);
        }
    }

    private PageRequest createPageRequest(Integer from, Integer size) {
        int pageNumber = from == null ? 0 : Math.max(from, 0);
        int pageSize = size == null ? 10 : Math.max(size, 0);
        return PageRequest.of(pageNumber, pageSize);
    }

    private long fetchViews(String uri, boolean unique) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String start = "2000-01-01 00:00:00";
        return statsClient.getStats(start, now, List.of(uri), unique)
                .stream()
                .findFirst()
                .map(StatDto::getHits)
                .orElse(0L);
    }
}
