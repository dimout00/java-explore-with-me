package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEvents(@RequestParam(required = false) List<Long> users,
                                           @RequestParam(required = false) List<String> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) LocalDateTime rangeStart,
                                           @RequestParam(required = false) LocalDateTime rangeEnd,
                                           @RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /admin/events: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        List<EventFullDto> result = eventService.searchEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        log.debug("GET /admin/events returned {} events", result.size());
        return result;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody UpdateEventAdminRequest request) {
        log.debug("PATCH /admin/events/{}: request={}", eventId, request);
        EventFullDto result = eventService.updateEventByAdmin(eventId, request);
        log.debug("PATCH /admin/events/{} updated event", eventId);
        return result;
    }
}