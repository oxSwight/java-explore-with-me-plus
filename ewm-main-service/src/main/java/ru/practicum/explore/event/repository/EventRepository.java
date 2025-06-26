package ru.practicum.explore.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    Optional<Event> findByCategoryId(long catId);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId")
    List<Event> findUserEvents(@Param("userId") long userId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.state = :state")
    Optional<Event> findEventByIdAndState(@Param("eventId") long eventId, @Param("state") String state);

    @Query("""
        SELECT e FROM Event e
        WHERE e.paid = :paid
          AND e.eventDate BETWEEN :start AND :end
          AND e.participantLimit IS NOT NULL
          AND e.state = :state
          AND (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
          AND e.category.id IN :categories
        """)
    List<Event> searchAvailablePaidEventsByTextAndCategory(@Param("paid") boolean paid,
                                                           @Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end,
                                                           @Param("state") String state,
                                                           @Param("text") String text,
                                                           @Param("categories") List<Long> categories,
                                                           Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.initiator.id IN :users
          AND e.state IN :states
          AND e.category.id IN :categories
          AND e.eventDate BETWEEN :start AND :end
        """)
    List<Event> findEventsByUsersStatesCategories(@Param("users") List<Long> users,
                                                  @Param("states") List<String> states,
                                                  @Param("categories") List<Long> categories,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.eventDate BETWEEN :start AND :end
        """)
    List<Event> findAllByDateRange(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.paid = :paid
          AND e.eventDate BETWEEN :start AND :end
          AND e.state = :state
          AND (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
        """)
    List<Event> searchPaidEventsWithText(@Param("paid") boolean paid,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("state") String state,
                                         @Param("text") String text,
                                         Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.eventDate BETWEEN :start AND :end
          AND e.state = :state
          AND (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
          AND e.category.id IN :categories
        """)
    List<Event> searchEventsByTextAndCategory(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("state") String state,
                                              @Param("text") String text,
                                              @Param("categories") List<Long> categories,
                                              Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.initiator.id IN :users
          AND e.eventDate BETWEEN :start AND :end
        """)
    List<Event> findEventsByUsers(@Param("users") List<Long> users,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.state IN :states
          AND e.eventDate BETWEEN :start AND :end
        """)
    List<Event> findEventsByStates(@Param("states") List<String> states,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.category.id IN :categories
          AND e.eventDate BETWEEN :start AND :end
        """)
    List<Event> findEventsByCategory(@Param("categories") List<Long> categories,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     Pageable pageable);
}
