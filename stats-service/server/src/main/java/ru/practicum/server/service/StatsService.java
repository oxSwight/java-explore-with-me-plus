package ru.practicum.server.service;

import ru.practicum.dto.EndHitDto;
import ru.practicum.dto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndHitDto hit(EndHitDto endpointHit);

    List<StatDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}