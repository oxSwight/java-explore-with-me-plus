package ru.practicum.explore.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.dto.EndHitDto;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class HitLoggingFilter extends OncePerRequestFilter {

    private final StatsClient statsClient;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        chain.doFilter(req, res);                            // сначала ответ клиенту

        if ("GET".equals(req.getMethod())
                && req.getRequestURI().startsWith("/events")) {

            EndHitDto hit = EndHitDto.builder()
                    .app("ewm-main-service")
                    .uri(req.getRequestURI())                // /events  или  /events/{id}
                    .ip(req.getRemoteAddr())
                    .timestamp(LocalDateTime.now().format(FMT))
                    .build();

            statsClient.save(hit);                           // отправили POST /hit
        }
    }
}

