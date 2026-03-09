package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.service.UserService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerUser(@Valid @RequestBody NewUserRequest request) {
        log.debug("POST /admin/users: request={}", request);
        UserDto result = userService.createUser(request);
        log.debug("POST /admin/users created user id={}", result.getId());
        return result;
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /admin/users: ids={}, from={}, size={}", ids, from, size);
        List<UserDto> result = userService.getUsers(ids, from, size);
        log.debug("GET /admin/users returned {} users", result.size());
        return result;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.debug("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
        log.debug("DELETE /admin/users/{} completed", userId);
    }
}