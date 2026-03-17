package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    // Публичные методы
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(page).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, page);
        }
        return compilations.stream()
                .map(this::toCompilationDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));
        return toCompilationDto(compilation);
    }

    // Админские методы
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        List<Event> events = new ArrayList<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = eventRepository.findAllByIds(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Some events not found");
            }
        }

        Compilation compilation = Compilation.builder()
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .title(dto.getTitle())
                .events(events)
                .build();

        compilation = compilationRepository.save(compilation);
        return toCompilationDto(compilation);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIds(request.getEvents());
            if (events.size() != request.getEvents().size()) {
                throw new NotFoundException("Some events not found");
            }
            compilation.setEvents(events);
        }

        compilation = compilationRepository.save(compilation);
        return toCompilationDto(compilation);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    private CompilationDto toCompilationDto(Compilation compilation) {
        List<Event> events = compilation.getEvents();
        var shortDtos = eventService.enrichEventsShort(events);
        return CompilationMapper.toCompilationDto(compilation, shortDtos);
    }
}