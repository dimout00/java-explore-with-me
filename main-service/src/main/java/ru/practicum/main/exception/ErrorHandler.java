package ru.practicum.main.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.practicum.main.dto.ApiError;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
        log.warn("Not found: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("The required object was not found.")
                .status(HttpStatus.NOT_FOUND)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException e) {
        log.warn("Conflict: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Integrity constraint has been violated.")
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException e) {
        log.warn("Forbidden: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Integrity constraint has been violated.")
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Method argument type mismatch: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message("Failed to convert value of type '" + e.getValue() + "' to required type '" + e.getRequiredType().getSimpleName() + "'")
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Http message not readable: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariable(MissingPathVariableException e) {
        log.warn("Missing path variable: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.warn("Missing request parameter: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleUnexpected(Throwable e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("Internal server error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException e) {
        log.warn("No handler found: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(e.getClass().getName()))
                .message(e.getMessage())
                .reason("The requested resource was not found")
                .status(HttpStatus.NOT_FOUND)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}