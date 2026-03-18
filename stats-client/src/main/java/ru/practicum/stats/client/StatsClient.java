package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    private final RestTemplate rest;
    private final String serverUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        this.serverUrl = serverUrl;
        this.rest = new RestTemplate();
    }

    public void hit(EndpointHit hit) {
        String url = serverUrl + "/hit";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHit> entity = new HttpEntity<>(hit, headers);
        try {
            ResponseEntity<Void> response = rest.postForEntity(url, entity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Hit sent successfully: {}", hit);
            } else {
                log.error("Failed to send hit, status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Ошибка при сохранении статистики: {}", e.getMessage(), e);
        }
    }

    public void hit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit hit = EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();
        this.hit(hit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            log.error("getStats called with null start or end: start={}, end={}", start, end);
            return List.of();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start)
                .queryParam("end", end);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }
        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        String url = builder.encode().toUriString();
        log.debug("Requesting stats from URL: {}", url);

        try {
            ResponseEntity<List<ViewStats>> response = rest.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Stats response: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Failed to get stats, status: {}", response.getStatusCode());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            return List.of();
        }
    }
}