package ru.practicum.main.controller.publik;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /categories: from={}, size={}", from, size);
        List<CategoryDto> result = categoryService.getCategories(from, size);
        log.debug("GET /categories returned {} categories", result.size());
        return result;
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.debug("GET /categories/{}", catId);
        CategoryDto result = categoryService.getCategory(catId);
        log.debug("GET /categories/{} returned category: {}", catId, result.getId());
        return result;
    }
}