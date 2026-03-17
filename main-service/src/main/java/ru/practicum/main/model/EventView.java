package ru.practicum.main.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_views")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 16)
    private String ip;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
}