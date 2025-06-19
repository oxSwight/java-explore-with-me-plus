package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.event.model.EventState;
import ru.practicum.explore.user.dto.UserDtoWithNoEmail;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;

    private CategoryDtoWithId category;
    private Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;

    private UserDtoWithNoEmail initiator;
    private Long views;
    private Long confirmedRequests;

    private Integer participantLimit;
    private Boolean requestModeration;

    private EventState state;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedOn;

    private LocationDto location;
}
