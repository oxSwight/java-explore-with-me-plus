package ru.practicum.explore.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.dto.StatDto;
import ru.practicum.explore.server.service.StatisticsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsServiceImpl service;

    @GetMapping("/stats")
    public ResponseEntity<List<StatDto>> getStatistics(@RequestParam()
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                       @RequestParam()
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                                       @RequestParam(required = false) List<String> uris,
                                                       @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получен запрос GET /stats");
        return ResponseEntity.ok(service.get(start, end, uris, unique));
    }

    @PostMapping("/hit")
    public ResponseEntity<EndHitDto> hit(@RequestBody EndHitDto endpointHit) {
        log.info("Получен запрос POST /hit");
        return new ResponseEntity<>(service.hit(endpointHit), HttpStatus.CREATED);
    }

}