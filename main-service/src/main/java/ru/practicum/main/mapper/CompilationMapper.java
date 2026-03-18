package ru.practicum.main.mapper;

import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.model.Compilation;

import java.util.List;

public class CompilationMapper {
    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(events)
                .build();
    }
}