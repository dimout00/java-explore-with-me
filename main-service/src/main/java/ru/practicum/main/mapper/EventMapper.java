package ru.practicum.main.mapper;

import ru.practicum.main.dto.*;
import ru.practicum.main.model.*;
import ru.practicum.main.dto.Location;
import java.time.LocalDateTime;

public class EventMapper {
    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static Event toEvent(NewEventDto dto, Category category, User initiator) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(initiator)
                .lat(dto.getLocation().getLat())
                .lon(dto.getLocation().getLon())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(EventState.PENDING)
                .title(dto.getTitle())
                .build();
    }
}