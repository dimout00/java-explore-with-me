package ru.practicum.main.mapper.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.practicum.main.dto.comment.CommentAuthorDto;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentEventDto;
import ru.practicum.main.dto.comment.NewCommentDto;
import ru.practicum.main.dto.comment.UpdateCommentDto;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.model.comment.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "editedOn", ignore = true)
    Comment toEntity(NewCommentDto dto);

    @Mapping(target = "author", source = "author", qualifiedByName = "toAuthorDto")
    @Mapping(target = "event", source = "event", qualifiedByName = "toEventDto")
    CommentDto toDto(Comment comment);

    @Named("toAuthorDto")
    default CommentAuthorDto toAuthorDto(User user) {
        if (user == null) return null;
        return CommentAuthorDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    @Named("toEventDto")
    default CommentEventDto toEventDto(Event event) {
        if (event == null) return null;
        return CommentEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .build();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "editedOn", ignore = true)
    void updateEntity(UpdateCommentDto dto, @MappingTarget Comment comment);
}