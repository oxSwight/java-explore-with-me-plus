package ru.practicum.explore.event.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "locations")
@Data
@ToString
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lat", nullable = true)
    private Float lat;

    @Column(name = "lon", nullable = true)
    private Float lon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        return id != null && id.equals(((Location) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}