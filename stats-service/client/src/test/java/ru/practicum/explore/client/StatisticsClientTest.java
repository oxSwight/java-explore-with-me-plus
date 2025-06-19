package ru.practicum.explore.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.dto.StatDto;
import ru.practicum.explore.tools.SimpleDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsClientTest {

    @Mock
    private RestTemplate httpTemplate;

    private StatsClient statisticsClient;

    @BeforeEach
    void init() {
        RestTemplateBuilder templateBuilder = mock(RestTemplateBuilder.class);
        when(templateBuilder.build()).thenReturn(httpTemplate);
        when(templateBuilder.uriTemplateHandler(any())).thenReturn(templateBuilder);
        when(templateBuilder.requestFactory(any(Supplier.class))).thenReturn(templateBuilder);

        String serviceUrl = "http://stats-service:8080";
        statisticsClient = new StatsClient(serviceUrl, templateBuilder);
    }

    @Test
    @DisplayName("Сохранение информации о запросе")
    void shouldPerformPostRequestWhenSavingHit() {
        EndHitDto hitData = new EndHitDto();
        hitData.setApp("analytics-service");
        hitData.setUri("/api/v1/data");
        hitData.setIp("192.168.1.10");
        hitData.setTimestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()));

        ResponseEntity<Object> mockResponse = ResponseEntity.status(201).build();
        when(httpTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(this::verifyHeaders),
                eq(Object.class))
        ).thenReturn(mockResponse);

        ResponseEntity<Object> result = statisticsClient.save(hitData);

        assertNotNull(result);
        assertEquals(mockResponse.getStatusCode(), result.getStatusCode());
        verify(httpTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(this::verifyHeaders),
                eq(Object.class));
    }

    @Test
    @DisplayName("Получение статистики с параметрами")
    void shouldPerformGetRequestWithAllParameters() {
        String beginTime = "2024-03-01 09:00:00";
        String finishTime = "2024-03-01 18:00:00";
        List<String> endpoints = List.of("/api/events", "/api/users");
        Boolean distinctIp = true;

        ResponseEntity<Object> mockResponse = ResponseEntity.ok().build();
        when(httpTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::verifyHeaders),
                eq(Object.class),
                eq(Map.of("start", beginTime, "end", finishTime,
                        "uris", String.join(",", endpoints),
                        "unique", distinctIp)))
        ).thenReturn(mockResponse);

        List<StatDto> result = statisticsClient.getStats(beginTime, finishTime, endpoints, distinctIp);

        assertNotNull(result);
        verify(httpTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::verifyHeaders),
                eq(Object.class),
                eq(Map.of("start", beginTime, "end", finishTime,
                        "uris", String.join(",", endpoints),
                        "unique", distinctIp)));
    }

    @Test
    @DisplayName("Получение статистики без указания URI")
    void shouldPerformGetRequestWithoutUriParameter() {
        String beginTime = "2024-04-15 00:00:00";
        String finishTime = "2024-04-16 23:59:59";
        List<String> endpoints = Collections.emptyList();
        Boolean distinctIp = false;

        ResponseEntity<Object> mockResponse = ResponseEntity.ok().build();
        when(httpTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::verifyHeaders),
                eq(Object.class),
                eq(Map.of("start", beginTime, "end", finishTime,
                        "uris", "",
                        "unique", distinctIp)))
        ).thenReturn(mockResponse);

        List<StatDto> result = statisticsClient.getStats(beginTime, finishTime, endpoints, distinctIp);

        assertNotNull(result);
        verify(httpTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::verifyHeaders),
                eq(Object.class),
                eq(Map.of("start", beginTime, "end", finishTime,
                        "uris", "",
                        "unique", distinctIp)));
    }

    @Test
    @DisplayName("Обработка ошибки сервера при получении статистики")
    void shouldHandleServerErrorDuringStatsRetrieval() {
        String beginTime = "2024-05-01 12:00:00";
        String finishTime = "2024-05-02 12:00:00";
        List<String> endpoints = List.of("/api/analytics");
        Boolean distinctIp = true;

        HttpStatusCodeException error = mock(HttpStatusCodeException.class);
        when(error.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        when(error.getResponseBodyAsByteArray()).thenReturn("Service unavailable".getBytes());

        when(httpTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::verifyHeaders),
                eq(Object.class),
                eq(Map.of("start", beginTime, "end", finishTime,
                        "uris", String.join(",", endpoints),
                        "unique", distinctIp)))
        ).thenThrow(error);

        List<StatDto> response = statisticsClient.getStats(beginTime, finishTime, endpoints, distinctIp);

        assertTrue(response.isEmpty());
    }

    private boolean verifyHeaders(HttpEntity<?> httpEntity) {
        HttpHeaders headers = httpEntity.getHeaders();
        return Objects.equals(headers.getContentType(), MediaType.APPLICATION_JSON) &&
                headers.getAccept().contains(MediaType.APPLICATION_JSON);
    }
}