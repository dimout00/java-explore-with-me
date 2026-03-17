package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.EventView;

public interface EventViewRepository extends JpaRepository<EventView, Long> {
    boolean existsByEventIdAndIp(Long eventId, String ip);
}