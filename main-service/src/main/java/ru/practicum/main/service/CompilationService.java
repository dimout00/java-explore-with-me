package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.*;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidationException;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService; // для обогащения событий

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllByIds(dto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(Collections.emptyList());
        }
        compilation = compilationRepository.save(compilation);
        return enrichCompilation(compilation);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        if (request.getTitle() != null) {
            if (request.getTitle().length() < 1 || request.getTitle().length() > 50) {
                throw new ValidationException("Title length must be between 1 and 50");
            }
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIds(request.getEvents());
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        return enrichCompilation(compilation);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        compilationRepository.delete(compilation);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(page).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, page);
        }
        return compilations.stream()
                .map(this::enrichCompilation)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        return enrichCompilation(compilation);
    }

    private CompilationDto enrichCompilation(Compilation compilation) {
        List<EventShortDto> eventDtos = eventService.enrichEventsShort(compilation.getEvents());
        return CompilationMapper.toCompilationDto(compilation, eventDtos);
    }
}