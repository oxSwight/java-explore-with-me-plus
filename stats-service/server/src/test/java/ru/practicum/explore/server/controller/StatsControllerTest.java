package ru.practicum.explore.server.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.explore.dto.EndHitDto;
import ru.practicum.explore.dto.StatDto;
import ru.practicum.explore.server.service.StatisticsServiceImpl;
import ru.practicum.explore.tools.SimpleDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class StatsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StatisticsServiceImpl service;

    @InjectMocks
    private StatisticsController statsController;

    private final EndHitDto hitDto = new EndHitDto();
    private final StatDto statsDto = new StatDto();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statsController).build();
        hitDto.setApp("test-app");
        hitDto.setUri("/test");
        hitDto.setIp("127.0.0.1");
        hitDto.setTimestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()));

        statsDto.setApp("test-app");
        statsDto.setUri("/test");
        statsDto.setHits(10L);
    }

    @Test
    void hitShouldReturnCreatedStatus() throws Exception {
        when(service.hit(any(EndHitDto.class))).thenReturn(hitDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"app\":\"test-app\",\"uri\":\"/test\",\"ip\":\"127.0.0.1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("test-app"))
                .andExpect(jsonPath("$.uri").value("/test"));
    }

    @Test
    void getStatsShouldReturnOkStatus() throws Exception {
        when(service.get(any(LocalDateTime.class), any(LocalDateTime.class), any(), any(Boolean.class)))
                .thenReturn(List.of(statsDto));

        mockMvc.perform(get("/stats")
                        .param("start", "2023-01-01 00:00:00")
                        .param("end", "2023-01-02 00:00:00")
                        .param("uris", "/test1", "/test2")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("test-app"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(10));
    }

    @Test
    void getStatsWithoutUrisShouldReturnOkStatus() throws Exception {
        when(service.get(any(LocalDateTime.class), any(LocalDateTime.class), any(), any(Boolean.class)))
                .thenReturn(List.of(statsDto));

        mockMvc.perform(get("/stats")
                        .param("start", "2023-01-01 00:00:00")
                        .param("end", "2023-01-02 00:00:00")
                        .param("unique", "true"))
                .andExpect(status().isOk());
    }
}