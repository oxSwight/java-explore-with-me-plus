package ru.practicum.explore.user.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.common.exception.ConflictException;
import ru.practicum.explore.common.exception.NotFoundException;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.dto.*;
import ru.practicum.explore.user.mapper.UserMapperNew;
import ru.practicum.explore.user.model.Request;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.RequestRepository;
import ru.practicum.explore.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        return UserMapperNew.mapToRequestDto(
                requestRepository.findByRequesterIdOrderByCreatedDateDesc(userId));
    }

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        int pageFrom = Objects.requireNonNullElse(from, 0);
        int pageSize = Objects.requireNonNullElse(size, 10);
        PageRequest page = PageRequest.of(pageFrom > 0 ? pageFrom / pageSize : 0, pageSize);
        if (ids == null || ids.isEmpty()) {
            return UserMapperNew.mapToUserDto(userRepository.findAll(page));
        }
        return UserMapperNew.mapToUserDto(userRepository.findAllById(ids));
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(long userId, long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if (!Objects.equals(request.getRequesterId(), userId)) {
            throw new ConflictException("Only requester may cancel own request");
        }

        // отменяем ТОЛЬКО PENDING-заявки
        if (!Statuses.PENDING.name().equals(request.getStatus())) {
            throw new ConflictException(
                    String.format("Request already %s — cannot cancel", request.getStatus()));
        }

        request.setStatus(Statuses.CANCELED.name());
        return UserMapperNew.mapToRequestDto(requestRepository.saveAndFlush(request));
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException();
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public RequestDto createRequest(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id=" + eventId + " not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id=" + userId + " not found"));
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new DataIntegrityViolationException("Duplicate request");
        }
        if (event.getInitiator().getId() == userId) {
            throw new DataIntegrityViolationException("Initiator cannot request own event");
        }
        if (!Statuses.PUBLISHED.name().equals(event.getState())) {
            throw new DataIntegrityViolationException("Event is not published");
        }
        if (event.getParticipantLimit() != 0 &&
                Objects.equals(event.getConfirmedRequests(), (long) event.getParticipantLimit())) {
            throw new DataIntegrityViolationException("Participant limit reached");
        }
        Request req = new Request();
        req.setEventId(eventId);
        req.setRequesterId(userId);
        req.setCreatedDate(LocalDateTime.now());
        if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            req.setStatus(Statuses.CONFIRMED.name());
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            req.setStatus(Statuses.PENDING.name());
        }
        return UserMapperNew.mapToRequestDto(requestRepository.save(req));
    }

    @Override
    @Transactional
    public UserDto createUser(@Valid UserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("E-mail already used");
        }
        User saved = userRepository.save(UserMapperNew.mapToUser(dto));
        return UserMapperNew.mapToUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto createUser(@Valid NewUserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("E-mail already used");
        }
        User saved = userRepository.save(UserMapperNew.mapToUser(dto));
        return UserMapperNew.mapToUserDto(saved);
    }

    @Override
    public Collection<RequestDto> getEventRequests(long userId, long eventId) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(EntityNotFoundException::new);
        return UserMapperNew.mapToRequestDto(
                requestRepository.findByEventId(eventId).orElse(List.of()));
    }

    private ResponseInformationAboutRequests buildUpdateResult(List<Request> requests) {

        List<RequestDto> confirmed = new ArrayList<>();
        List<RequestDto> rejected  = new ArrayList<>();
        List<RequestDto> pending   = new ArrayList<>();

        for (Request r : requests) {
            RequestDto dto = UserMapperNew.mapToRequestDto(r);
            switch (r.getStatus()) {
                case "CONFIRMED" -> confirmed.add(dto);
                case "REJECTED"  -> rejected.add(dto);
                default          -> pending.add(dto);       // PENDING / CANCELED и т.д.
            }
        }
        return new ResponseInformationAboutRequests(confirmed, rejected, pending);
    }

    @Override
    @Transactional
    public ResponseInformationAboutRequests changeRequestsStatuses(
            long userId, long eventId, ChangedStatusOfRequestsDto dto) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<Request> requests =
                (List<Request>) requestRepository.findByIdInAndEventId(dto.getRequestIds(), eventId);

        if (requests.isEmpty()) {
            throw new ConflictException("No requests found for update");
        }

        // 1. все должны быть PENDING
        if (requests.stream().anyMatch(r -> !Statuses.PENDING.name().equals(r.getStatus()))) {
            throw new ConflictException("Only pending requests may be changed");
        }

        // 2. проверяем лимит, если хотим CONFIRMED
        if (Statuses.CONFIRMED.name().equals(dto.getStatus())) {
            long freeSlots = event.getParticipantLimit() == 0
                    ? Long.MAX_VALUE
                    : event.getParticipantLimit() - event.getConfirmedRequests();
            if (freeSlots < requests.size()) {
                throw new ConflictException("Participant limit exceeded");
            }
        }

        // 3. применяем
        requests.forEach(r -> r.setStatus(dto.getStatus()));
        requestRepository.saveAll(requests);

        // 4. обновляем счётчик confirm-ов, если нужно
        if (Statuses.CONFIRMED.name().equals(dto.getStatus())) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            eventRepository.save(event);
        }

        return buildUpdateResult(requests);   // ваш каскадный мап-метод
    }
}
