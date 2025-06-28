package ru.practicum.explore.comments.mapper;

import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;
import ru.practicum.explore.comments.dto.NewCommentDto;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.comments.model.Comment;
import ru.practicum.explore.comments.dto.CommentDto;

@UtilityClass
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUser().getId());
        dto.setEventId(comment.getEvent().getId());
        dto.setCreatedOn(comment.getCreatedOn());

        return dto;
    }

    public static Comment fromNewCommentDto(NewCommentDto newCommentDto, User user, Event event) {
        if (newCommentDto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setUser(user);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());

        return comment;
    }
}