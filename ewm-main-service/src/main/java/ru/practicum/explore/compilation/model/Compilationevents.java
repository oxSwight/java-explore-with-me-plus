package ru.practicum.explore.compilation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compilationevents")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Compilationevents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compilation_id", nullable = false)
    private Long compilationId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compilationevents)) return false;
        return id != null && id.equals(((Compilationevents) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}