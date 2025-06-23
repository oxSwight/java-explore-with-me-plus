package ru.practicum.explore.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.event.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, String status);

    // Находим все заявки по списку ID
    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    // Находим заявки по ID события
    List<ParticipationRequest> findByEventId(Long eventId);

}
