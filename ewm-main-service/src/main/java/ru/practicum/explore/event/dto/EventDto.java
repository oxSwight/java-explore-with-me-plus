package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore.category.dto.CategoryDtoWithId;
import ru.practicum.explore.user.dto.UserDtoWithNoEmail;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    private Long id;
    private String annotation;
    private CategoryDtoWithId category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private UserDtoWithNoEmail initiator;
    private Boolean paid;
    private String title;
    private Long confirmedRequests;
    private Long views;
    private String description;
    private LocationDto location;
    private Integer participantLimit;
    private Boolean requestModeration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private String state;
}
