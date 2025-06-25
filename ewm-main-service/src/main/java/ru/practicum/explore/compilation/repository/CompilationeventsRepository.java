package ru.practicum.explore.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.compilation.model.Compilationevents;

import java.util.Collection;

@Repository
public interface CompilationeventsRepository extends JpaRepository<Compilationevents, Long> {
    Collection<Compilationevents> findByCompilationId(long id);
}