package ru.practicum.main.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.NewCommentDto;
import ru.practicum.main.dto.comment.UpdateCommentDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.comment.CommentMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.User;
import ru.practicum.main.model.comment.Comment;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.repository.comment.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        log.debug("Adding comment by user {} to event {}", userId, eventId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }
        Comment comment = commentMapper.toEntity(dto);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        log.info("Comment added with id: {}", saved.getId());
        return commentMapper.toDto(saved);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        log.debug("Updating comment {} by user {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User is not the author of this comment");
        }
        commentMapper.updateEntity(dto, comment);
        comment.setEditedOn(LocalDateTime.now());
        Comment updated = commentRepository.save(comment);
        log.info("Comment updated with id: {}", updated.getId());
        return commentMapper.toDto(updated);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.debug("Deleting comment {} by user {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User is not the author of this comment");
        }
        commentRepository.delete(comment);
        log.info("Comment deleted by user {}: {}", userId, commentId);
    }

    @Transactional
    public void adminDeleteComment(Long commentId) {
        log.debug("Admin deleting comment {}", commentId);
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
        log.info("Admin deleted comment: {}", commentId);
    }

    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        log.debug("Getting comments for event {} from={} size={}", eventId, from, size);
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found with id: " + eventId);
        }
        PageRequest page = PageRequest.of(from / size, size);
        return commentRepository.findAllByEventId(eventId, page).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByUser(Long userId, int from, int size) {
        log.debug("Getting comments by user {} from={} size={}", userId, from, size);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        PageRequest page = PageRequest.of(from / size, size);
        return commentRepository.findAllByAuthorId(userId, page).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public CommentDto getCommentById(Long commentId) {
        log.debug("Getting comment by id: {}", commentId);
        return commentRepository.findById(commentId)
                .map(commentMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
    }
}