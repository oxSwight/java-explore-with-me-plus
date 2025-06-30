package ru.practicum.explore.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.dto.StatDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatsClient extends RestServiceClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public StatsClient(String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(
                        HttpClients.createDefault()))
                .build());
    }

    public ResponseEntity<Object> save(EndHitDto hit) {
        return submit("/hit", hit);
    }

    public List<StatDto> getStats(String start, String end,
                                  List<String> uris, boolean unique) {

        Map<String, Object> params = Map.of(
                "start", start,
                "end", end,
                "uris", String.join(",", uris),
                "unique", unique
        );

        ResponseEntity<Object> resp = fetch(
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}", params);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            return MAPPER.convertValue(resp.getBody(), new TypeReference<>() {});
        }
        return Collections.emptyList();
    }
}
