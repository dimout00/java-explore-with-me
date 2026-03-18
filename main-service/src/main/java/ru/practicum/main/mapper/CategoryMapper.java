package ru.practicum.main.mapper;

import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.model.Category;

public class CategoryMapper {
    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }
}