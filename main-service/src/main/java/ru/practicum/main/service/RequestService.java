package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.RequestRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
                .orElseThrow(() -> new NotFoundException("User not found"));
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event must be published");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }
        long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(event.isRequestModeration() && event.getParticipantLimit() != 0
                        ? RequestStatus.PENDING
                        : RequestStatus.CONFIRMED)
                .build();

        request = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found for this user");
        }
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new ConflictException("Cannot cancel confirmed request");
        }
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found for this user");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest update) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found for this user");
        }
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Request moderation is disabled for this event");
        }

        List<Request> requests = requestRepository.findAllById(update.getRequestIds());
        for (Request r : requests) {
            if (!r.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request " + r.getId() + " does not belong to this event");
            }
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request " + r.getId() + " is not in PENDING state");
            }
        }

        long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
        long limit = event.getParticipantLimit();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        if (update.getStatus().equals("CONFIRMED")) {
            for (Request r : requests) {
                if (limit > 0 && confirmedCount >= limit) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.toParticipationRequestDto(r));
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.toParticipationRequestDto(r));
                    confirmedCount++;
                }
            }
        } else if (update.getStatus().equals("REJECTED")) {
            for (Request r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(r));
            }
        } else {
            throw new IllegalArgumentException("Unknown status: " + update.getStatus());
        }

        requestRepository.saveAll(requests);
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }
}