package ru.practicum.main.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private CommentAuthorDto author;
    private CommentEventDto event;
    private LocalDateTime createdOn;
    private LocalDateTime editedOn;
}