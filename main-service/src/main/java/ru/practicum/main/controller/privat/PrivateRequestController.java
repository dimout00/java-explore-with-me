package ru.practicum.main.controller.privat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.debug("GET /users/{}/requests", userId);
        List<ParticipationRequestDto> result = requestService.getUserRequests(userId);
        log.debug("GET /users/{}/requests returned {} requests", userId, result.size());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @RequestParam Long eventId) {
        log.debug("POST /users/{}/requests?eventId={}", userId, eventId);
        ParticipationRequestDto result = requestService.createRequest(userId, eventId);
        log.debug("POST /users/{}/requests created request with id={}", userId, result.getId());
        return result;
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.debug("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);
        log.debug("PATCH /users/{}/requests/{}/cancel canceled request", userId, requestId);
        return result;
    }
}