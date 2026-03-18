package ru.practicum.main.controller.privat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.*;
import ru.practicum.main.service.EventService;
import ru.practicum.main.service.RequestService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /users/{}/events: from={}, size={}", userId, from, size);
        List<EventShortDto> result = eventService.getUserEvents(userId, from, size);
        log.debug("GET /users/{}/events returned {} events", userId, result.size());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto dto) {
        log.debug("POST /users/{}/events: dto={}", userId, dto);
        EventFullDto result = eventService.createEvent(userId, dto);
        log.debug("POST /users/{}/events created event id={}", userId, result.getId());
        return result;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        log.debug("GET /users/{}/events/{}", userId, eventId);
        EventFullDto result = eventService.getUserEventById(userId, eventId);
        log.debug("GET /users/{}/events/{} returned event", userId, eventId);
        return result;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest request) {
        log.debug("PATCH /users/{}/events/{}: request={}", userId, eventId, request);
        EventFullDto result = eventService.updateEventByUser(userId, eventId, request);
        log.debug("PATCH /users/{}/events/{} updated event", userId, eventId);
        return result;
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        log.debug("GET /users/{}/events/{}/requests", userId, eventId);
        List<ParticipationRequestDto> result = requestService.getEventRequests(userId, eventId);
        log.debug("GET /users/{}/events/{}/requests returned {} requests", userId, eventId, result.size());
        return result;
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest update) {
        log.debug("PATCH /users/{}/events/{}/requests: update={}", userId, eventId, update);
        EventRequestStatusUpdateResult result = requestService.updateRequestStatus(userId, eventId, update);
        log.debug("PATCH /users/{}/events/{}/requests updated requests: confirmed={}, rejected={}",
                userId, eventId, result.getConfirmedRequests().size(), result.getRejectedRequests().size());
        return result;
    }
}