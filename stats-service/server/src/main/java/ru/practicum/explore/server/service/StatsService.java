package ru.practicum.explore.server.service;

import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.dto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndHitDto hit(EndHitDto endpointHit);

    List<StatDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}