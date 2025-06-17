package ru.practicum.explore.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    Optional<Event> findByCategoryId(long catId);

    List<Event> findByInitiatorId(long userId, Pageable pageable);

    Optional<Event> findByIdAndState(long eventId, String state);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(boolean paid, LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, List<Long> catId, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(boolean paid, LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, List<Long> catId, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(boolean paid, LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(boolean paid, LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(boolean paid, LocalDateTime start, LocalDateTime end, String state1, List<Long> catId, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(boolean paid, LocalDateTime start, LocalDateTime end, String state1, List<Long> catId, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(boolean paid, LocalDateTime start, LocalDateTime end, String state1, Pageable pageable);

    List<Event> findByPaidAndEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(boolean paid, LocalDateTime start, LocalDateTime end, String state1, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, List<Long> catId, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCaseAndCategoryIdIn(LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, List<Long> catId, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndAnnotationIgnoreCaseOrDescriptionIgnoreCase(LocalDateTime start, LocalDateTime end, String state1, String text1, String text2, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndStateAndCategoryIdIn(LocalDateTime start, LocalDateTime end, String state1, List<Long> catId, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndStateAndCategoryIdIn(LocalDateTime start, LocalDateTime end, String state1, List<Long> catId, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndParticipantLimitNotNullAndState(LocalDateTime start, LocalDateTime end, String state1, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqualAndState(LocalDateTime start, LocalDateTime end, String state1, Pageable pageable);

    List<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<Long> users, List<String> states, List<Long> categories, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByInitiatorIdInAndStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<Long> users, List<String> states, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByInitiatorIdInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<Long> users, List<Long> categories, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByInitiatorIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<Long> users, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByStateInAndCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<String> states, List<Long> categories, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByStateInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<String> states, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByCategoryIdInAndEventDateGreaterThanEqualAndEventDateLessThanEqual(List<Long> categories, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findByEventDateGreaterThanEqualAndEventDateLessThanEqual(LocalDateTime start, LocalDateTime end, Pageable pageable);
}