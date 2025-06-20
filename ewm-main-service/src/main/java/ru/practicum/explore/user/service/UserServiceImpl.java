package ru.practicum.explore.user.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.dto.*;
import ru.practicum.explore.user.mapper.UserMapperNew;
import ru.practicum.explore.user.model.Request;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.RequestRepository;
import ru.practicum.explore.user.repository.UserRepository;
import ru.practicum.explore.user.dto.NewUserDto;
import ru.practicum.explore.common.exception.NotFoundException;
import ru.practicum.explore.common.exception.ConflictException;

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
        userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);
        return UserMapperNew.mapToRequestDto(
                requestRepository.findByRequesterIdOrderByCreatedDateDesc(userId));
    }

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (ids == null || ids.isEmpty()) {
            return UserMapperNew.mapToUserDto(userRepository.findAll(page));
        }
        return UserMapperNew.mapToUserDto(userRepository.findAllById(ids));
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(long userId, long requestId) {
        userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(EntityNotFoundException::new);

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

        /* 1. базовые проверки существования сущностей -------------- */
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));
        /* 2. бизнес-ограничения ------------------------------------ */
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Повторный запрос не допускается");          // 409
        }
        if (event.getInitiator().getId() == userId) {
            throw new ConflictException("Инициатор не может подать заявку на своё событие"); // 409
        }
        if (!Statuses.PUBLISHED.name().equals(event.getState())) {
            throw new ConflictException("Нельзя подать заявку на неопубликованное событие"); // 409
        }
        if (event.getParticipantLimit() != 0 &&
                Objects.equals(event.getConfirmedRequests(), (long) event.getParticipantLimit())) {
            throw new ConflictException("Лимит участников достигнут");                        // 409
        }

        /* 3. создание заявки --------------------------------------- */
        Request request = new Request();
        request.setEventId(event.getId());
        request.setRequesterId(user.getId());

        if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            request.setStatus(Statuses.CONFIRMED.name());
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            request.setStatus(Statuses.PENDING.name());
        }

        return UserMapperNew.mapToRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public UserDto createUser(@Valid UserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("E-mail уже используется");
        }
        User saved = userRepository.save(UserMapperNew.mapToUser(dto));
        return UserMapperNew.mapToUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto createUser(@Valid NewUserDto dto) {

        /* проверка уникальности email */
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("E-mail уже используется");
        }

        /* маппинг + сохранение */
        User saved = userRepository.save(UserMapperNew.mapToUser(dto));
        return UserMapperNew.mapToUserDto(saved);
    }

    @Override
    public Collection<RequestDto> getEventRequests(long userId, long eventId) {
        // проверяем, что событие принадлежит пользователю (можно расширить логику при необходимости)
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(EntityNotFoundException::new);
        return UserMapperNew.mapToRequestDto(
                requestRepository.findByEventId(eventId).orElse(List.of()));
    }

    @Override
    @Transactional
    public ResponseInformationAboutRequests changeRequestsStatuses(long userId,
                                                                   long eventId,
                                                                   ChangedStatusOfRequestsDto dto) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(EntityNotFoundException::new);

        long limit = event.getParticipantLimit();
        long confirmed = event.getConfirmedRequests();

        Collection<Request> requests =
                requestRepository.findByIdInAndEventId(dto.getRequestIds(), eventId);

        if (requests.isEmpty()) {
            throw new DataIntegrityViolationException("Запросы не найдены");
        }

        for (Request r : requests) {
            if (!Statuses.PENDING.name().equals(r.getStatus())) {
                continue;                       // только из PENDING можно менять
            }

            if (Statuses.REJECTED.name().equals(dto.getStatus())) {
                r.setStatus(Statuses.REJECTED.name());
            } else if (Statuses.CONFIRMED.name().equals(dto.getStatus())) {
                if (limit != 0 && confirmed >= limit) {
                    throw new DataIntegrityViolationException("Лимит подтверждений достигнут");
                }
                r.setStatus(Statuses.CONFIRMED.name());
                confirmed++;
            }
            requestRepository.save(r);
        }

        event.setConfirmedRequests(confirmed);
        eventRepository.save(event);

        ResponseInformationAboutRequests resp = new ResponseInformationAboutRequests();
        resp.setConfirmedRequests(UserMapperNew.mapToRequestDto(
                requestRepository.findByEventIdAndStatus(eventId, Statuses.CONFIRMED.name())));
        resp.setRejectedRequests(UserMapperNew.mapToRequestDto(
                requestRepository.findByEventIdAndStatus(eventId, Statuses.REJECTED.name())));
        resp.setPendingRequests(UserMapperNew.mapToRequestDto(
                requestRepository.findByEventIdAndStatus(eventId, Statuses.PENDING.name())));
        return resp;
    }
}
