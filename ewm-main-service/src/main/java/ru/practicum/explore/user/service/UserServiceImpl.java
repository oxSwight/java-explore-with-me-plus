package ru.practicum.explore.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.dto.ChangedStatusOfRequestsDto;
import ru.practicum.explore.user.dto.RequestDto;
import ru.practicum.explore.user.dto.ResponseInformationAboutRequests;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.mapper.UserMapperNew;
import ru.practicum.explore.user.model.Request;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.RequestRepository;
import ru.practicum.explore.user.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    @Override
    public Collection<RequestDto> getUserRequests(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent())
            return UserMapperNew.mapToRequestDto(requestRepository.findByRequesterIdOrderByCreatedDateDesc(userId));
        else throw new EntityNotFoundException();
    }

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (ids.equals(List.of(0L))) return UserMapperNew.mapToUserDto(userRepository.findAll(page));
        else {
            return new ArrayList<>(UserMapperNew.mapToUserDto(userRepository.findAllById(ids)));
        }
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(long userId, long requestId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Optional<Request> request = requestRepository.findById(requestId);
            if (request.isPresent()) {
                Request canceledRequest = request.get();
                canceledRequest.setStatus(Statuses.CANCELED.name());
                return UserMapperNew.mapToRequestDto(requestRepository.saveAndFlush(canceledRequest));
            } else throw new EntityNotFoundException();
        } else throw new EntityNotFoundException();
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) userRepository.deleteById(userId);
        else throw new EntityNotFoundException();
    }

    @Override
    @Transactional
    public RequestDto createRequest(long userId, long eventId) {
        Request request = new Request();
        Optional<Event> event = eventRepository.findById(eventId);
        Optional<User> user = userRepository.findById(userId);
        Optional<Request> request1 = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (request1.isPresent() || (event.isPresent() && event.get().getInitiator().getId().equals(userId)) || (event.isPresent() && !event.get().getState().equals(Statuses.PUBLISHED.name())) || (event.isPresent() && Long.valueOf(event.get().getParticipantLimit()).equals(event.get().getConfirmedRequests()) && !event.get().getParticipantLimit().equals(0)))
            throw new DataIntegrityViolationException("Data integrity violation exception");
        if (event.isPresent() && event.get().getInitiator().getId() != userId && user.isPresent()) {
            Event event1;
            request.setEventId(event.get().getId());
            request.setRequesterId(user.get().getId());
            if (!event.get().getRequestModeration() || event.get().getParticipantLimit().equals(0)) {
                request.setStatus(Statuses.CONFIRMED.name());
                event1 = event.get();
                event1.setConfirmedRequests(event1.getConfirmedRequests() + 1L);
                eventRepository.save(event1);
            } else request.setStatus(Statuses.PENDING.name());
            return UserMapperNew.mapToRequestDto(requestRepository.save(request));
        } else throw new EntityNotFoundException();
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        Optional<User> email = userRepository.findByEmail(userDto.getEmail());
        if (email.isPresent()) throw new DataIntegrityViolationException("Data integrity violation exception");
        User user = userRepository.save(UserMapperNew.mapToUser(userDto));
        return UserMapperNew.mapToUserDto(user);
    }

    @Override
    public Collection<RequestDto> getEventRequests(long userId, long eventId) {
        return UserMapperNew.mapToRequestDto(requestRepository.findByEventId(eventId).get());
    }

    @Override
    @Transactional
    public ResponseInformationAboutRequests changeRequestsStatuses(long userId, long eventId, ChangedStatusOfRequestsDto changedStatusOfRequestsDto) {
        Optional<Event> event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event.isPresent()) {
            Event event1 = event.get();
            Long limit = Long.valueOf(event.get().getParticipantLimit());
            Long iterable = event.get().getConfirmedRequests();
            Collection<Request> requests = requestRepository.findByIdInAndEventId(changedStatusOfRequestsDto.getRequestIds(), eventId);
            if (!requests.isEmpty()) {
                for (Request request : requests) {
                    if (limit - iterable == 0L)
                        throw new DataIntegrityViolationException("Data integrity violation exception");
                    if (request.getStatus().equals(Statuses.PENDING.name())) {
                        if (changedStatusOfRequestsDto.getStatus().equals(Statuses.REJECTED.name()))
                            request.setStatus(changedStatusOfRequestsDto.getStatus());
                        if (changedStatusOfRequestsDto.getStatus().equals(Statuses.CONFIRMED.name())) {
                            request.setStatus(changedStatusOfRequestsDto.getStatus());
                            event1.setConfirmedRequests(event1.getConfirmedRequests() + 1L);
                            event1 = eventRepository.saveAndFlush(event1);
                            iterable = event1.getConfirmedRequests();
                        }
                        requestRepository.saveAndFlush(request);
                    }
                }
            } else throw new DataIntegrityViolationException("Data integrity violation exception");
        } else throw new EntityNotFoundException();
        ResponseInformationAboutRequests response = new ResponseInformationAboutRequests();
        response.setConfirmedRequests(UserMapperNew.mapToRequestDto(requestRepository.findByEventIdAndStatus(eventId, Statuses.CONFIRMED.name())));
        response.setRejectedRequests(UserMapperNew.mapToRequestDto(requestRepository.findByEventIdAndStatus(eventId, Statuses.REJECTED.name())));
        response.setPendingRequests(UserMapperNew.mapToRequestDto(requestRepository.findByEventIdAndStatus(eventId, Statuses.PENDING.name())));
        return response;
    }
}