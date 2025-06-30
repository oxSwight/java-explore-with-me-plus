package ru.practicum.explore.event.repository;

import org.springframework.data.domain.Page;
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

    Page<Event> findByInitiatorId(long userId, Pageable pageable);

    Optional<Event> findByIdAndState(long eventId, String state);

    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%'))) AND e.category.id IN :catId
            """)
    List<Event> findPaidWithLimitStateTextAndCategory(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                                      @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                                      @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%'))) AND e.category.id IN :catId
            """)
    List<Event> findPaidStateTextAndCategory(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                             @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                             @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%')))
            """)
    List<Event> findPaidWithLimitStateText(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                           @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                           Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%')))
            """)
    List<Event> findPaidStateText(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                  @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                  Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state AND 
            e.category.id IN :catId
            """)
    List<Event> findPaidWithLimitStateCategory(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                               @Param("state") String state1, @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state AND e.category.id IN :catId
            """)
    List<Event> findPaidStateCategory(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                      @Param("state") String state1, @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state
            """)
    List<Event> findPaidWithLimitState(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                       @Param("state") String state1, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.paid = :paid AND e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state
            """)
    List<Event> findPaidState(@Param("paid") boolean paid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                              @Param("state") String state1, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%'))) AND e.category.id IN :catId
            """)
    List<Event> findLimitStateTextAndCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                              @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                              @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%'))) AND e.category.id IN :catId
            """)
    List<Event> findStateTextAndCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                         @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                         @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%')))
            """)
    List<Event> findLimitStateText(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                   @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                                   Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state AND 
            (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text2, '%')))
            """)
    List<Event> findStateText(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                              @Param("state") String state1, @Param("text1") String text1, @Param("text2") String text2,
                              Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state AND e.category.id IN :catId
            """)
    List<Event> findLimitStateCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                       @Param("state") String state1, @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state AND e.category.id IN :catId
            """)
    List<Event> findStateCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                  @Param("state") String state1, @Param("catId") List<Long> catId, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.participantLimit IS NOT NULL AND e.state = :state
            """)
    List<Event> findLimitState(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                               @Param("state") String state1, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end AND e.state = :state
            """)
    List<Event> findState(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                          @Param("state") String state1, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.state IN :states AND e.category.id IN :categories AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findUsersStatesCategories(@Param("users") List<Long> users, @Param("states") List<String> states,
                                          @Param("categories") List<Long> categories, @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.state IN :states AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findUsersStates(@Param("users") List<Long> users, @Param("states") List<String> states,
                                @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.category.id IN :categories AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findUsersCategories(@Param("users") List<Long> users, @Param("categories") List<Long> categories,
                                    @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findUsersEvents(@Param("users") List<Long> users, @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.state IN :states AND e.category.id IN :categories AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findStatesCategories(@Param("states") List<String> states, @Param("categories") List<Long> categories,
                                     @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.state IN :states AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findStates(@Param("states") List<String> states, @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.category.id IN :categories AND e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findCategories(@Param("categories") List<Long> categories, @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end, Pageable pageable);


    @Query("""
            SELECT e FROM Event e WHERE e.eventDate >= :start AND e.eventDate <= :end
            """)
    List<Event> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
