package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.server.model.HitEntity;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository repository;

    @Transactional
    public void saveHit(EndpointHit hit) {
        log.info("Saving hit: app={}, uri={}, ip={}, timestamp={}",
                hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp());
        HitEntity entity = HitEntity.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
        repository.save(entity);
        log.debug("Hit saved with id: {}", entity.getId());
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.debug("getStats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (uris != null && uris.isEmpty()) {
            log.debug("Empty uris list, returning empty result");
            return Collections.emptyList();
        }

        List<ViewStats> result;
        if (Boolean.TRUE.equals(unique)) {
            result = repository.findAllUniqueByDateRange(start, end, uris);
        } else {
            result = repository.findAllByDateRange(start, end, uris);
        }

        log.debug("getStats result: {}", result);
        return result;
    }
}