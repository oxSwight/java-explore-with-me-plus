package ru.practicum.explore.user.service;

import ru.practicum.explore.user.dto.ChangedStatusOfRequestsDto;
import ru.practicum.explore.user.dto.RequestDto;
import ru.practicum.explore.user.dto.ResponseInformationAboutRequests;
import ru.practicum.explore.user.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {
    Collection<RequestDto> getUserRequests(long userId);

    Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size);

    RequestDto cancelRequest(long userId, long requestId);

    void deleteUser(long userId);

    RequestDto createRequest(long userId, long eventId);

    UserDto createUser(UserDto userDto);

    Collection<RequestDto> getEventRequests(long userId, long eventId);

    ResponseInformationAboutRequests changeRequestsStatuses(long userId, long eventId, ChangedStatusOfRequestsDto changedStatusOfRequestsDto);
}