package ru.practicum.explore.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.dto.StatDto;
import ru.practicum.explore.server.model.EndHitMapper;
import ru.practicum.explore.server.model.StatsMapper;
import ru.practicum.explore.server.repository.EndpointHitsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatsService {
    private final EndpointHitsRepository endpointHitsRepository;

    public EndHitDto hit(EndHitDto endpointHit) {
        return EndHitMapper.toEndpointHitDto(
                endpointHitsRepository.save(EndHitMapper.toEndpointHit(endpointHit))
        );
    }

    public List<StatDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        if (Boolean.TRUE.equals(unique)) {
            return endpointHitsRepository.findUniqueStats(start, end, uris)
                    .stream()
                    .map(StatsMapper::toStatsDto)
                    .toList();
        } else {
            return endpointHitsRepository.findStats(start, end, uris)
                    .stream()
                    .map(StatsMapper::toStatsDto)
                    .toList();
        }
    }
}