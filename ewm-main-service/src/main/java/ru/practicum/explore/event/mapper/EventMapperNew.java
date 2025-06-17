package ru.practicum.explore.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.LocationDto;
import ru.practicum.explore.event.dto.PatchEventDto;
import ru.practicum.explore.event.dto.ResponseEventDto;
import ru.practicum.explore.user.dto.*;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.model.Location;
import ru.practicum.explore.user.model.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapperNew {
    public static CategoryDtoWithId mapToCategoryDtoWithId(Category category) {
        CategoryDtoWithId categoryDtoWithId = new CategoryDtoWithId();
        categoryDtoWithId.setId(category.getId());
        categoryDtoWithId.setName(category.getName());
        return categoryDtoWithId;
    }

    public static UserDtoWithNoEmail mapToUserDtoWithNoEmail(User user) {
        UserDtoWithNoEmail userDtoWithNoEmail = new UserDtoWithNoEmail();
        userDtoWithNoEmail.setId(user.getId());
        userDtoWithNoEmail.setName(user.getName());
        return userDtoWithNoEmail;
    }

    public static LocationDto mapToLocationDto(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        return locationDto;
    }

    public static Location mapToLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }

    public static EventDto mapToEventDto(Event event) {
        EventDto eventDto = new EventDto();
        eventDto.setAnnotation(event.getAnnotation());
        eventDto.setId(event.getId());
        eventDto.setCategory(mapToCategoryDtoWithId(event.getCategory()));
        eventDto.setEventDate(event.getEventDate());
        eventDto.setInitiator(mapToUserDtoWithNoEmail(event.getInitiator()));
        eventDto.setPaid(event.getPaid());
        eventDto.setTitle(event.getTitle());
        eventDto.setConfirmedRequests(event.getConfirmedRequests());
        eventDto.setViews(event.getViews());
        eventDto.setDescription(event.getDescription());
        eventDto.setLocation(mapToLocationDto(event.getLocation()));
        eventDto.setParticipantLimit(event.getParticipantLimit());
        eventDto.setRequestModeration(event.getRequestModeration());
        eventDto.setCreatedOn(event.getCreatedOn());
        eventDto.setPublishedOn(event.getPublishedOn());
        eventDto.setState(event.getState());
        return eventDto;
    }

    public static List<EventDto> mapToEventDto(Iterable<Event> events) {
        List<EventDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(mapToEventDto(event));
        }
        return result;
    }

    public static List<ResponseEventDto> mapToResponseEventDto(Iterable<Event> events) {
        List<ResponseEventDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(mapToResponseEventDto(event));
        }
        return result;
    }

    public static ResponseEventDto mapToResponseEventDto(Event event) {
        ResponseEventDto eventDto = new ResponseEventDto();
        eventDto.setAnnotation(event.getAnnotation());
        eventDto.setId(event.getId());
        eventDto.setCategory(mapToCategoryDtoWithId(event.getCategory()));
        eventDto.setEventDate(event.getEventDate());
        eventDto.setInitiator(mapToUserDtoWithNoEmail(event.getInitiator()));
        eventDto.setPaid(event.getPaid());
        eventDto.setTitle(event.getTitle());
        eventDto.setConfirmedRequests(event.getConfirmedRequests());
        eventDto.setViews(event.getViews());
        return eventDto;
    }

    public static Event changeEvent(Event event, PatchEventDto patchEventDto) {
        if (patchEventDto.getAnnotation() != null) event.setAnnotation(patchEventDto.getAnnotation());
        if (patchEventDto.getDescription() != null) event.setDescription(patchEventDto.getDescription());
        if (patchEventDto.getEventDate() != null) event.setEventDate(patchEventDto.getEventDate());
        if (patchEventDto.getPaid() != null) event.setPaid(patchEventDto.getPaid());
        if (patchEventDto.getParticipantLimit() != null && patchEventDto.getParticipantLimit() >= 0)
            event.setParticipantLimit(patchEventDto.getParticipantLimit());
        if (patchEventDto.getRequestModeration() != null)
            event.setRequestModeration(patchEventDto.getRequestModeration());
        if (patchEventDto.getStateAction() != null) event.setState(patchEventDto.getStateAction());
        if (patchEventDto.getTitle() != null) event.setTitle(patchEventDto.getTitle());
        return event;
    }
}