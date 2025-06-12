package ru.practicum.server.model;

import ru.practicum.dto.StatDto;

public class StatsMapper {

    private StatsMapper() {
    }

    public static StatDto toStatsDto(Stats stats) {
        return StatDto.builder()
                .app(stats.getApp())
                .uri(stats.getUri())
                .hits(stats.getHits())
                .build();
    }

    public static Stats toStats(StatDto statsDto) {
        return Stats.builder()
                .app(statsDto.getApp())
                .uri(statsDto.getUri())
                .hits(statsDto.getHits())
                .build();
    }
}