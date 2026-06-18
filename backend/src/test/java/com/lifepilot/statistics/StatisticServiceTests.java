package com.lifepilot.statistics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.finance.TransactionCategoryMapper;
import com.lifepilot.finance.TransactionRecord;
import com.lifepilot.finance.TransactionRecordMapper;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.inventory.InventoryItemMapper;
import com.lifepilot.shopping.ShoppingItemMapper;
import com.lifepilot.shopping.ShoppingListMapper;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.statistics.dto.InventoryStatsResponse;
import com.lifepilot.statistics.dto.OverviewResponse;
import com.lifepilot.statistics.dto.TodoStatsResponse;
import com.lifepilot.todo.TodoTask;
import com.lifepilot.todo.TodoTaskMapper;

@ExtendWith(MockitoExtension.class)
class StatisticServiceTests {

    @Mock
    private HouseholdService householdService;
    @Mock
    private TransactionRecordMapper transactionRecordMapper;
    @Mock
    private TransactionCategoryMapper transactionCategoryMapper;
    @Mock
    private InventoryItemMapper inventoryItemMapper;
    @Mock
    private ShoppingListMapper shoppingListMapper;
    @Mock
    private ShoppingItemMapper shoppingItemMapper;
    @Mock
    private TodoTaskMapper todoTaskMapper;

    @InjectMocks
    private StatisticService statisticService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;

    // --- getOverview ---

    @Test
    void getOverview_emptySpace_returnsZeroes() {
        when(transactionRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(inventoryItemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(shoppingListMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(inventoryItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        OverviewResponse result = statisticService.getOverview(USER_ID, SPACE_ID);

        assertEquals(BigDecimal.ZERO, result.totalIncome());
        assertEquals(BigDecimal.ZERO, result.totalExpense());
        assertEquals(BigDecimal.ZERO, result.netBalance());
        assertEquals(0, result.transactionCount());
        assertEquals(0L, result.inventoryItemCount());
        assertEquals(0L, result.shoppingListCount());
        assertEquals(0L, result.inventoryAlertCount());
    }

    @Test
    void getOverview_calculatesIncomeAndExpense() {
        TransactionRecord income = new TransactionRecord();
        income.setType("income");
        income.setAmount(new BigDecimal("5000"));

        TransactionRecord expense = new TransactionRecord();
        expense.setType("expense");
        expense.setAmount(new BigDecimal("1200"));

        when(transactionRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(income, expense));
        when(inventoryItemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(shoppingListMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(inventoryItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        OverviewResponse result = statisticService.getOverview(USER_ID, SPACE_ID);

        assertEquals(new BigDecimal("5000"), result.totalIncome());
        assertEquals(new BigDecimal("1200"), result.totalExpense());
        assertEquals(new BigDecimal("3800"), result.netBalance());
        assertEquals(2, result.transactionCount());
    }

    @Test
    void getOverview_detectsLowStock() {
        InventoryItem lowItem = new InventoryItem();
        lowItem.setQuantity(new BigDecimal("2"));
        lowItem.setLowStockThreshold(new BigDecimal("5"));

        InventoryItem okItem = new InventoryItem();
        okItem.setQuantity(new BigDecimal("10"));
        okItem.setLowStockThreshold(new BigDecimal("5"));

        when(transactionRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(inventoryItemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(shoppingListMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(inventoryItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(lowItem, okItem));

        OverviewResponse result = statisticService.getOverview(USER_ID, SPACE_ID);

        assertEquals(1L, result.inventoryAlertCount());
    }

    // --- getInventoryStats ---

    @Test
    void getInventoryStats_empty_returnsZeroes() {
        when(inventoryItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        InventoryStatsResponse result = statisticService.getInventoryStats(USER_ID, SPACE_ID);

        assertEquals(0, result.totalItems());
        assertEquals(0L, result.lowStockCount());
        assertTrue(result.byCategory().isEmpty());
    }

    @Test
    void getInventoryStats_groupsByCategory() {
        InventoryItem food1 = new InventoryItem();
        food1.setCategory("食品");
        food1.setQuantity(new BigDecimal("10"));
        food1.setLowStockThreshold(new BigDecimal("3"));

        InventoryItem food2 = new InventoryItem();
        food2.setCategory("食品");
        food2.setQuantity(new BigDecimal("1"));
        food2.setLowStockThreshold(new BigDecimal("5"));

        InventoryItem daily = new InventoryItem();
        daily.setCategory("日用品");
        daily.setQuantity(new BigDecimal("20"));
        daily.setLowStockThreshold(new BigDecimal("5"));

        when(inventoryItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(food1, food2, daily));

        InventoryStatsResponse result = statisticService.getInventoryStats(USER_ID, SPACE_ID);

        assertEquals(3, result.totalItems());
        assertEquals(1L, result.lowStockCount()); // food2 is below threshold
        assertFalse(result.byCategory().isEmpty());
    }

    // --- getTodoStats ---

    @Test
    void getTodoStats_empty_returnsZeroes() {
        when(todoTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        TodoStatsResponse result = statisticService.getTodoStats(USER_ID, SPACE_ID);

        assertEquals(0, result.totalCount());
        assertEquals(0L, result.pendingCount());
        assertEquals(0L, result.inProgressCount());
        assertEquals(0L, result.completedCount());
        assertEquals(0L, result.cancelledCount());
        assertEquals(0L, result.overdueCount());
    }

    @Test
    void getTodoStats_countsStatusesAndOverdue() {
        TodoTask pending = new TodoTask();
        pending.setStatus("pending");
        pending.setDueAt(LocalDateTime.now().minusDays(1)); // overdue

        TodoTask inProgress = new TodoTask();
        inProgress.setStatus("in_progress");
        inProgress.setDueAt(null);

        TodoTask completed = new TodoTask();
        completed.setStatus("completed");
        completed.setDueAt(null);

        TodoTask cancelled = new TodoTask();
        cancelled.setStatus("cancelled");
        cancelled.setDueAt(null);

        when(todoTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(pending, inProgress, completed, cancelled));

        TodoStatsResponse result = statisticService.getTodoStats(USER_ID, SPACE_ID);

        assertEquals(4, result.totalCount());
        assertEquals(1L, result.pendingCount());
        assertEquals(1L, result.inProgressCount());
        assertEquals(1L, result.completedCount());
        assertEquals(1L, result.cancelledCount());
        assertEquals(1L, result.overdueCount()); // pending with past due date
    }

    @Test
    void getTodoStats_nonPendingNotOverdue() {
        TodoTask completedWithPastDue = new TodoTask();
        completedWithPastDue.setStatus("completed");
        completedWithPastDue.setDueAt(LocalDateTime.now().minusDays(1));

        when(todoTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(completedWithPastDue));

        TodoStatsResponse result = statisticService.getTodoStats(USER_ID, SPACE_ID);

        assertEquals(0L, result.overdueCount()); // completed tasks are not overdue
    }
}