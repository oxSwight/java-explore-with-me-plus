package ru.practicum.explore.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.global.dto.Statuses;
import ru.practicum.explore.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annotation", nullable = false)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "event_date", nullable = true)
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @Column(name = "paid", nullable = true)
    private Boolean paid;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "confirmed_requests", nullable = true)
    private Long confirmedRequests = 0L;

    @Column(name = "views", nullable = true)
    private Long views;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "participant_limit", nullable = true)
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Column(name = "created_On", nullable = true)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "published_On", nullable = true)
    private LocalDateTime publishedOn;

    @Column(name = "state", nullable = true)
    private String state = Statuses.PENDING.name();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        return id != null && id.equals(((Event) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}