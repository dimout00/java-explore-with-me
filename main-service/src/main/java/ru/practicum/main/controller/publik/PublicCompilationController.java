package ru.practicum.main.controller.publik;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /compilations: pinned={}, from={}, size={}", pinned, from, size);
        List<CompilationDto> result = compilationService.getCompilations(pinned, from, size);
        log.debug("GET /compilations returned {} compilations", result.size());
        return result;
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.debug("GET /compilations/{}", compId);
        CompilationDto result = compilationService.getCompilation(compId);
        log.debug("GET /compilations/{} returned compilation: {}", compId, result.getId());
        return result;
    }
}