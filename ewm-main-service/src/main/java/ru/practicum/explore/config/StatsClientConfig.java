package ru.practicum.explore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explore.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Value("${stats-service.url}")
    private String statsUrl;

    @Bean
    public StatsClient statsClient(RestTemplateBuilder builder) {
        return new StatsClient(statsUrl, builder);
    }
}
