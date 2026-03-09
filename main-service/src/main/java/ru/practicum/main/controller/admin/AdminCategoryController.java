package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.service.CategoryService;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto dto) {
        log.debug("POST /admin/categories: dto={}", dto);
        CategoryDto result = categoryService.createCategory(dto);
        log.debug("POST /admin/categories created category id={}", result.getId());
        return result;
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto dto) {
        log.debug("PATCH /admin/categories/{}: dto={}", catId, dto);
        CategoryDto result = categoryService.updateCategory(catId, dto);
        log.debug("PATCH /admin/categories/{} updated category", catId);
        return result;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.debug("DELETE /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
        log.debug("DELETE /admin/categories/{} completed", catId);
    }
}