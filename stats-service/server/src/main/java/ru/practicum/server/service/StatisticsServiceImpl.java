package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndHitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.server.model.EndHitMapper;
import ru.practicum.server.model.StatsMapper;
import ru.practicum.server.repository.EndpointHitsRepository;

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