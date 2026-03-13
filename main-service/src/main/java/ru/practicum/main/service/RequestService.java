package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.*;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.RequestRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating request for userId={}, eventId={}", userId, eventId);

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Initiator cannot request participation: userId={}, eventId={}", userId, eventId);
            throw new ConflictException("Initiator cannot request participation");
        }
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Event not published: eventId={}, state={}", eventId, event.getState());
            throw new ConflictException("Event must be published");
        }
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            log.warn("Request already exists: userId={}, eventId={}", userId, eventId);
            throw new ConflictException("Request already exists");
        }

        int participantLimit = event.getParticipantLimit();
        if (participantLimit > 0) {
            Long confirmed = requestRepository.countConfirmedRequestsByEventId(eventId);
            long confirmedCount = confirmed != null ? confirmed : 0L;
            if (confirmedCount >= participantLimit) {
                log.warn("Participant limit reached: eventId={}, limit={}, confirmed={}", eventId, participantLimit, confirmedCount);
                throw new ConflictException("Participant limit reached");
            }
        }

        boolean requestModeration = event.isRequestModeration();
        RequestStatus status = requestModeration && participantLimit != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED;

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();
        request = requestRepository.save(request);

        requestRepository.flush();
        request = requestRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload request"));

        log.info("Request created: id={}, status={}, created={}", request.getId(), request.getStatus(), request.getCreated());
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found for this user");
        }
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not owned by user");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest update) {
        log.info("Updating request status for eventId={} by userId={}, requestIds={}, status={}",
                eventId, userId, update.getRequestIds(), update.getStatus());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not owned by user");
        }

        List<Request> requests = requestRepository.findAllById(update.getRequestIds());
        for (Request r : requests) {
            if (!r.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Request not for this event");
            }
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        Long confirmedObj = requestRepository.countConfirmedRequestsByEventId(eventId);
        long confirmedCount = confirmedObj != null ? confirmedObj : 0L;

        long limit = event.getParticipantLimit(); // примитив
        List<Request> confirmed = new java.util.ArrayList<>();
        List<Request> rejected = new java.util.ArrayList<>();

        if (update.getStatus().equals("CONFIRMED")) {
            for (Request r : requests) {
                if (limit > 0 && confirmedCount >= limit) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(r);
                    log.debug("Request {} rejected due to limit", r.getId());
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(r);
                    confirmedCount++;
                    log.debug("Request {} confirmed", r.getId());
                }
            }
            if (limit > 0 && confirmedCount >= limit) {
                List<Request> pending = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
                for (Request r : pending) {
                    if (!update.getRequestIds().contains(r.getId())) {
                        r.setStatus(RequestStatus.REJECTED);
                        rejected.add(r);
                        log.debug("Request {} automatically rejected due to limit reached", r.getId());
                    }
                }
            }
        } else if (update.getStatus().equals("REJECTED")) {
            for (Request r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(r);
                log.debug("Request {} rejected by user", r.getId());
            }
        }

        requestRepository.saveAll(confirmed);
        requestRepository.saveAll(rejected);

        log.info("Request status update completed: confirmed={}, rejected={}", confirmed.size(), rejected.size());
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                .rejectedRequests(rejected.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                .build();
    }
}