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
public class ResponseEventDto {

    private Long id;
    private String title;
    private String annotation;
    private CategoryDtoWithId category;
    private Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserDtoWithNoEmail initiator;
    private Long views;
    private Long confirmedRequests;

    private String description;
    private Integer participantLimit;
    private String state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private LocationDto location;
    private Boolean requestModeration;
}
