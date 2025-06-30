package ru.practicum.explore.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.dto.EndHitDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HitLoggingFilter extends OncePerRequestFilter {

    private final StatsClient statsClient;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        /* сохраняем данные запроса, пока они ещё доступны */
        boolean logThisRequest =
                "GET".equals(req.getMethod()) &&
                        req.getRequestURI().startsWith("/events");

        String uri = req.getRequestURI();
        String ip  = Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                .orElse(req.getRemoteAddr());

        chain.doFilter(req, res);

        if (logThisRequest) {
            EndHitDto hit = EndHitDto.builder()
                    .app("ewm-main-service")
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now().format(FMT))
                    .build();
            try {
                statsClient.save(hit);
            } catch (Exception e) {
                log.warn("Cannot send hit to stats: {}", e.getMessage());
            }
        }
    }
}
