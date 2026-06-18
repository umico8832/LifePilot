package com.lifepilot.todo;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifepilot.common.ApiResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.security.CurrentUserPrincipal;
import com.lifepilot.todo.dto.CreateTodoRequest;
import com.lifepilot.todo.dto.TodoResponse;
import com.lifepilot.todo.dto.UpdateTodoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spaces/{spaceId}/todo-tasks")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ApiResponse<TodoResponse> createTask(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @Valid @RequestBody CreateTodoRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(todoService.createTask(principal.id(), spaceId, request));
    }

    @GetMapping
    public ApiResponse<List<TodoResponse>> listTasks(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @RequestParam(required = false) String status) {
        requireAuth(principal);
        return ApiResponse.ok(todoService.listTasks(principal.id(), spaceId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<TodoResponse> getTask(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        return ApiResponse.ok(todoService.getTask(principal.id(), spaceId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<TodoResponse> updateTask(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTodoRequest request) {
        requireAuth(principal);
        return ApiResponse.ok(todoService.updateTask(principal.id(), spaceId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(
            @AuthenticationPrincipal CurrentUserPrincipal principal,
            @PathVariable Long spaceId,
            @PathVariable Long id) {
        requireAuth(principal);
        todoService.deleteTask(principal.id(), spaceId, id);
        return ApiResponse.ok(null);
    }

    private void requireAuth(CurrentUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Authentication required");
        }
    }
}