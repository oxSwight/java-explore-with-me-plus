package ru.practicum.explore.user.service;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.user.dto.*;

import java.util.Collection;
import java.util.List;

public interface UserService {
    Collection<RequestDto> getUserRequests(long userId);

    Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size);

    RequestDto cancelRequest(long userId, long requestId);

    void deleteUser(long userId);

    RequestDto createRequest(long userId, long eventId);

    @Transactional
    UserDto createUser(@Valid UserDto dto);

    UserDto createUser(@Valid NewUserDto userDto);

    Collection<RequestDto> getEventRequests(long userId, long eventId);

    ResponseInformationAboutRequests changeRequestsStatuses(long userId, long eventId, ChangedStatusOfRequestsDto changedStatusOfRequestsDto);
}