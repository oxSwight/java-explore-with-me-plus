package ru.practicum.explore.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId")
    List<Comment> getAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId")
    List<Comment> getAllByUserId(@Param("userId") Long userId);
}