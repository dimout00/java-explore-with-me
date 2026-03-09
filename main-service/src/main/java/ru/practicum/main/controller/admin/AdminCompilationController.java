package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.service.CompilationService;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto dto) {
        log.debug("POST /admin/compilations: dto={}", dto);
        CompilationDto result = compilationService.createCompilation(dto);
        log.debug("POST /admin/compilations created compilation id={}", result.getId());
        return result;
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest request) {
        log.debug("PATCH /admin/compilations/{}: request={}", compId, request);
        CompilationDto result = compilationService.updateCompilation(compId, request);
        log.debug("PATCH /admin/compilations/{} updated compilation", compId);
        return result;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.debug("DELETE /admin/compilations/{}", compId);
        compilationService.deleteCompilation(compId);
        log.debug("DELETE /admin/compilations/{} completed", compId);
    }
}