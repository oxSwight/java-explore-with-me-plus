package ru.practicum.explore.server.model;

import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.tools.SimpleDateTimeFormatter;

import java.time.LocalDateTime;

public class EndHitMapper {
    private EndHitMapper() {

    }

    public static EndpointHit toEndpointHit(EndHitDto endpointHitDto) {
        return EndpointHit.builder()
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static EndHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndHitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(SimpleDateTimeFormatter.toString(endpointHit.getTimestamp()))
                .build();
    }
}