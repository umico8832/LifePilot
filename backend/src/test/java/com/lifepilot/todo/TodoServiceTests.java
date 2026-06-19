package com.lifepilot.todo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.todo.dto.CreateTodoRequest;
import com.lifepilot.todo.dto.TodoResponse;
import com.lifepilot.todo.dto.UpdateTodoRequest;

@ExtendWith(MockitoExtension.class)
class TodoServiceTests {

    @Mock
    private TodoTaskMapper todoMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private TodoService todoService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long TASK_ID = 500L;

    // --- createTask ---

    @Test
    void createTask_success_defaultsPriority() {
        CreateTodoRequest request = new CreateTodoRequest(
                "买菜", "去超市买蔬菜", null, null, null, null
        );

        TodoResponse response = todoService.createTask(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(todoMapper).insert((TodoTask) any());
        assertNotNull(response);
    }

    @Test
    void createTask_withExplicitPriority() {
        CreateTodoRequest request = new CreateTodoRequest(
                "紧急任务", null, "urgent",
                LocalDateTime.now().plusDays(1), null, null
        );

        TodoResponse response = todoService.createTask(USER_ID, SPACE_ID, request);

        assertNotNull(response);
        verify(todoMapper).insert((TodoTask) any());
    }

    @Test
    void createTask_invalidPriority_throwsException() {
        CreateTodoRequest request = new CreateTodoRequest(
                "任务", null, "super_high", null, null, null
        );

        assertThrows(BusinessException.class,
                () -> todoService.createTask(USER_ID, SPACE_ID, request));
        verify(todoMapper, never()).insert((TodoTask) any());
    }

    @Test
    void createTask_nonMember_throwsException() {
        doThrow(new BusinessException("FORBIDDEN", "Not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        CreateTodoRequest request = new CreateTodoRequest("任务", null, null, null, null, null);

        assertThrows(BusinessException.class,
                () -> todoService.createTask(USER_ID, SPACE_ID, request));
    }

    // --- listTasks ---

    @Test
    void listTasks_returnsResults() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(SPACE_ID);
        task.setTitle("买菜");
        task.setStatus("pending");

        when(todoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(task));

        List<TodoResponse> result = todoService.listTasks(USER_ID, SPACE_ID, null);

        assertEquals(1, result.size());
    }

    @Test
    void listTasks_withStatusFilter() {
        when(todoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<TodoResponse> result = todoService.listTasks(USER_ID, SPACE_ID, "completed");

        assertTrue(result.isEmpty());
    }

    // --- getTask ---

    @Test
    void getTask_found() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(SPACE_ID);
        task.setTitle("买菜");
        task.setStatus("pending");

        when(todoMapper.selectById(TASK_ID)).thenReturn(task);

        TodoResponse result = todoService.getTask(USER_ID, SPACE_ID, TASK_ID);

        assertNotNull(result);
    }

    @Test
    void getTask_notFound_throwsException() {
        when(todoMapper.selectById(TASK_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> todoService.getTask(USER_ID, SPACE_ID, TASK_ID));
    }

    @Test
    void getTask_wrongSpace_throwsException() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(999L);

        when(todoMapper.selectById(TASK_ID)).thenReturn(task);

        assertThrows(BusinessException.class,
                () -> todoService.getTask(USER_ID, SPACE_ID, TASK_ID));
    }

    // --- updateTask ---

    @Test
    void updateTask_success() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(SPACE_ID);
        task.setTitle("旧标题");
        task.setStatus("pending");

        when(todoMapper.selectById(TASK_ID)).thenReturn(task);

        UpdateTodoRequest request = new UpdateTodoRequest(
                "新标题", "新描述", "in_progress", "high", null, null, null
        );

        TodoResponse result = todoService.updateTask(USER_ID, SPACE_ID, TASK_ID, request);

        assertNotNull(result);
        verify(todoMapper).updateById((TodoTask) any());
    }

    @Test
    void updateTask_invalidStatus_throwsException() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(SPACE_ID);
        task.setTitle("任务");
        task.setStatus("pending");

        when(todoMapper.selectById(TASK_ID)).thenReturn(task);

        UpdateTodoRequest request = new UpdateTodoRequest(
                null, null, "invalid_status", null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> todoService.updateTask(USER_ID, SPACE_ID, TASK_ID, request));
        verify(todoMapper, never()).updateById((TodoTask) any());
    }

    @Test
    void updateTask_invalidPriority_throwsException() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(SPACE_ID);
        task.setTitle("任务");
        task.setStatus("pending");

        when(todoMapper.selectById(TASK_ID)).thenReturn(task);

        UpdateTodoRequest request = new UpdateTodoRequest(
                null, null, null, "mega", null, null, null
        );

        assertThrows(BusinessException.class,
                () -> todoService.updateTask(USER_ID, SPACE_ID, TASK_ID, request));
        verify(todoMapper, never()).updateById((TodoTask) any());
    }

    @Test
    void updateTask_notFound_throwsException() {
        when(todoMapper.selectById(TASK_ID)).thenReturn(null);

        UpdateTodoRequest request = new UpdateTodoRequest(
                null, null, null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> todoService.updateTask(USER_ID, SPACE_ID, TASK_ID, request));
    }

    // --- deleteTask ---

    @Test
    void deleteTask_success() {
        TodoTask task = new TodoTask();
        task.setId(TASK_ID);
        task.setHouseholdId(SPACE_ID);

        when(todoMapper.selectById(TASK_ID)).thenReturn(task);

        todoService.deleteTask(USER_ID, SPACE_ID, TASK_ID);

        verify(todoMapper).deleteById((Long) eq(TASK_ID));
    }

    @Test
    void deleteTask_notFound_throwsException() {
        when(todoMapper.selectById(TASK_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> todoService.deleteTask(USER_ID, SPACE_ID, TASK_ID));
        verify(todoMapper, never()).deleteById((Long) any());
    }
}