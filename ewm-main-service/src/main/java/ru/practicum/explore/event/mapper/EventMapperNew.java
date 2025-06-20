package ru.practicum.explore.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.model.EventState;
import ru.practicum.explore.event.model.Location;
import ru.practicum.explore.user.dto.UserDtoWithNoEmail;
import ru.practicum.explore.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapperNew {

    /* ---------- helpers для вложенных сущностей ---------- */

    public static CategoryDtoWithId mapToCategoryDtoWithId(Category category) {
        CategoryDtoWithId dto = new CategoryDtoWithId();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    public static UserDtoWithNoEmail mapToUserDtoWithNoEmail(User user) {
        UserDtoWithNoEmail dto = new UserDtoWithNoEmail();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    public static LocationDto mapToLocationDto(Location location) {
        LocationDto dto = new LocationDto();
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());
        return dto;
    }

    public static Location mapToLocation(LocationDto dto) {
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    public static EventDto mapToEventDto(Event event) {
        EventDto dto = new EventDto();
        dto.setAnnotation(event.getAnnotation());
        dto.setId(event.getId());
        dto.setCategory(mapToCategoryDtoWithId(event.getCategory()));
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(mapToUserDtoWithNoEmail(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setViews(event.getViews());
        dto.setDescription(event.getDescription());
        dto.setLocation(mapToLocationDto(event.getLocation()));
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setState(event.getState());
        return dto;
    }

    public static List<EventDto> mapToEventDto(Iterable<Event> events) {
        List<EventDto> res = new ArrayList<>();
        for (Event e : events) res.add(mapToEventDto(e));
        return res;
    }

    public static ResponseEventDto mapToResponseEventDto(Event event) {
        if (event == null) {
            return null;
        }

        ResponseEventDto dto = new ResponseEventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(mapToCategoryDtoWithId(event.getCategory()));
        dto.setPaid(event.getPaid());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(mapToUserDtoWithNoEmail(event.getInitiator()));
        dto.setViews(event.getViews());
        dto.setConfirmedRequests(event.getConfirmedRequests());

        dto.setDescription(event.getDescription());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setState(event.getState());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setLocation(mapToLocationDto(event.getLocation()));
        dto.setRequestModeration(event.getRequestModeration());

        return dto;
    }

    public static List<ResponseEventDto> mapToResponseEventDto(Iterable<Event> events) {
        List<ResponseEventDto> res = new ArrayList<>();
        for (Event e : events) res.add(mapToResponseEventDto(e));
        return res;
    }

    public static Event toEntity(NewEventDto dto,
                                 User initiator,
                                 Category category,
                                 Location location) {

        Event e = new Event();
        e.setTitle(dto.getTitle());
        e.setAnnotation(dto.getAnnotation());
        e.setDescription(dto.getDescription());
        e.setEventDate(dto.getEventDate());
        e.setCategory(category);
        e.setInitiator(initiator);
        e.setLocation(location);

        e.setPaid(dto.getPaid() != null && dto.getPaid());
        e.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        e.setRequestModeration(dto.getRequestModeration() == null || dto.getRequestModeration());

        e.setState(String.valueOf(EventState.PENDING));
        e.setCreatedOn(LocalDateTime.now());
        e.setViews(0L);
        e.setConfirmedRequests(0L);

        return e;
    }

    /* ---------- entity → полный dto ---------- */

    public static EventFullDto toFullDto(Event e) {
        return EventFullDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .annotation(e.getAnnotation())
                .description(e.getDescription())
                .category(mapToCategoryDtoWithId(e.getCategory()))
                .paid(e.getPaid())
                .eventDate(e.getEventDate())
                .initiator(mapToUserDtoWithNoEmail(e.getInitiator()))
                .views(e.getViews())
                .confirmedRequests(e.getConfirmedRequests())
                .participantLimit(e.getParticipantLimit())
                .requestModeration(e.getRequestModeration())
                .state(EventState.valueOf(e.getState()))
                .createdOn(e.getCreatedOn())
                .publishedOn(e.getPublishedOn())
                .location(mapToLocationDto(e.getLocation()))
                .build();
    }

    /* ---------- patch helper ---------- */

    public static Event changeEvent(Event event, PatchEventDto patch) {
        if (patch.getAnnotation() != null)          event.setAnnotation(patch.getAnnotation());
        if (patch.getDescription() != null)         event.setDescription(patch.getDescription());
        if (patch.getEventDate() != null)           event.setEventDate(patch.getEventDate());
        if (patch.getPaid() != null)                event.setPaid(patch.getPaid());
        if (patch.getParticipantLimit() != null &&
                patch.getParticipantLimit() >= 0)       event.setParticipantLimit(patch.getParticipantLimit());
        if (patch.getRequestModeration() != null)   event.setRequestModeration(patch.getRequestModeration());
        if (patch.getStateAction() != null)         event.setState(patch.getStateAction());
        if (patch.getTitle() != null)               event.setTitle(patch.getTitle());
        return event;
    }
}
