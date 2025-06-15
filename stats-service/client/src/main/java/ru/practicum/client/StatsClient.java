package ru.practicum.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndHitDto;
import ru.practicum.dto.StatDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatsClient extends RestServiceClient {
    @Autowired
    public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());
    }

    public List<StatDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        String urisParam = String.join(",", uris);
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", urisParam,
                "unique", unique
        );
        ResponseEntity<Object> response = fetch("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(response.getBody(),
                    new TypeReference<List<StatDto>>() {});
        }
        return Collections.emptyList();
    }

    public ResponseEntity<Object> save(EndHitDto endpointHit) {
        return submit("/hit", endpointHit);
    }
}