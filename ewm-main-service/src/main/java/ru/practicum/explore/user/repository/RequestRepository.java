package ru.practicum.explore.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.user.model.Request;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    Collection<Request> findByRequesterIdOrderByCreatedDateDesc(long userId);

    Optional<Request> findByRequesterIdAndEventId(long userId, long eventId);

    Collection<Request> findByEventIdAndStatus(long userId, String status);

    Collection<Request> findByIdInAndEventId(List<Long> ids, long eventId);

    Optional<Collection<Request>> findByEventId(long eventId);
}