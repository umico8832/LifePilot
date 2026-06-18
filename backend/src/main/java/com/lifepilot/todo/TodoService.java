package com.lifepilot.todo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.todo.dto.CreateTodoRequest;
import com.lifepilot.todo.dto.TodoResponse;
import com.lifepilot.todo.dto.UpdateTodoRequest;

@Service
public class TodoService {

    private static final Set<String> VALID_STATUSES = Set.of("pending", "in_progress", "completed", "cancelled");
    private static final Set<String> VALID_PRIORITIES = Set.of("low", "medium", "high", "urgent");

    private final TodoTaskMapper todoMapper;
    private final HouseholdService householdService;

    public TodoService(TodoTaskMapper todoMapper, HouseholdService householdService) {
        this.todoMapper = todoMapper;
        this.householdService = householdService;
    }

    @Transactional
    public TodoResponse createTask(Long userId, Long spaceId, CreateTodoRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        TodoTask task = new TodoTask();
        task.setHouseholdId(spaceId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus("pending");

        String priority = request.priority();
        if (priority != null && !VALID_PRIORITIES.contains(priority)) {
            throw new BusinessException("INVALID_PRIORITY", "Priority must be one of: " + VALID_PRIORITIES);
        }
        task.setPriority(priority != null ? priority : "medium");

        task.setDueAt(request.dueAt());
        task.setRepeatRule(request.repeatRule());
        task.setAssignedTo(request.assignedTo());

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        todoMapper.insert(task);
        return TodoResponse.from(task);
    }

    public List<TodoResponse> listTasks(Long userId, Long spaceId, String status) {
        householdService.requireSpaceMembership(userId, spaceId);

        LambdaQueryWrapper<TodoTask> wrapper = new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getHouseholdId, spaceId);

        if (status != null && !status.isBlank()) {
            wrapper.eq(TodoTask::getStatus, status);
        }

        wrapper.orderByAsc(TodoTask::getStatus)
               .orderByDesc(TodoTask::getPriority)
               .orderByAsc(TodoTask::getDueAt)
               .orderByDesc(TodoTask::getCreatedAt);

        List<TodoTask> tasks = todoMapper.selectList(wrapper);
        return tasks.stream().map(TodoResponse::from).collect(Collectors.toList());
    }

    public TodoResponse getTask(Long userId, Long spaceId, Long taskId) {
        householdService.requireSpaceMembership(userId, spaceId);

        TodoTask task = todoMapper.selectById(taskId);
        if (task == null || !task.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Todo task not found");
        }

        return TodoResponse.from(task);
    }

    @Transactional
    public TodoResponse updateTask(Long userId, Long spaceId, Long taskId, UpdateTodoRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        TodoTask task = todoMapper.selectById(taskId);
        if (task == null || !task.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Todo task not found");
        }

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());

        if (request.status() != null) {
            if (!VALID_STATUSES.contains(request.status())) {
                throw new BusinessException("INVALID_STATUS", "Status must be one of: " + VALID_STATUSES);
            }
            task.setStatus(request.status());
        }

        if (request.priority() != null) {
            if (!VALID_PRIORITIES.contains(request.priority())) {
                throw new BusinessException("INVALID_PRIORITY", "Priority must be one of: " + VALID_PRIORITIES);
            }
            task.setPriority(request.priority());
        }

        if (request.dueAt() != null) task.setDueAt(request.dueAt());
        if (request.repeatRule() != null) task.setRepeatRule(request.repeatRule());
        if (request.assignedTo() != null) task.setAssignedTo(request.assignedTo());
        task.setUpdatedAt(LocalDateTime.now());

        todoMapper.updateById(task);
        return TodoResponse.from(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long spaceId, Long taskId) {
        householdService.requireSpaceMembership(userId, spaceId);

        TodoTask task = todoMapper.selectById(taskId);
        if (task == null || !task.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Todo task not found");
        }

        todoMapper.deleteById(taskId);
    }
}