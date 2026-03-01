package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.server.model.HitEntity;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository repository;

    public void saveHit(EndpointHit hit) {
        HitEntity entity = HitEntity.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
        repository.save(entity);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Неверный диапазон дат");
        }
        if (Boolean.TRUE.equals(unique)) {
            return repository.findAllUniqueByDateRange(start, end, uris);
        } else {
            return repository.findAllByDateRange(start, end, uris);
        }
    }
}