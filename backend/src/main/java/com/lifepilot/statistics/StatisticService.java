package com.lifepilot.statistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.finance.TransactionCategory;
import com.lifepilot.finance.TransactionCategoryMapper;
import com.lifepilot.finance.TransactionRecord;
import com.lifepilot.finance.TransactionRecordMapper;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.inventory.InventoryItemMapper;
import com.lifepilot.shopping.ShoppingItem;
import com.lifepilot.shopping.ShoppingItemMapper;
import com.lifepilot.shopping.ShoppingList;
import com.lifepilot.shopping.ShoppingListMapper;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.todo.TodoTask;
import com.lifepilot.todo.TodoTaskMapper;
import com.lifepilot.statistics.dto.FinanceCategoriesResponse;
import com.lifepilot.statistics.dto.FinanceMonthlyResponse;
import com.lifepilot.statistics.dto.InventoryAlertsResponse;
import com.lifepilot.statistics.dto.InventoryStatsResponse;
import com.lifepilot.statistics.dto.OverviewResponse;
import com.lifepilot.statistics.dto.ShoppingStatsResponse;
import com.lifepilot.statistics.dto.TodoStatsResponse;

@Service
public class StatisticService {

    private final HouseholdService householdService;
    private final TransactionRecordMapper transactionRecordMapper;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingItemMapper shoppingItemMapper;
    private final TodoTaskMapper todoTaskMapper;

    public StatisticService(HouseholdService householdService,
                            TransactionRecordMapper transactionRecordMapper,
                            TransactionCategoryMapper transactionCategoryMapper,
                            InventoryItemMapper inventoryItemMapper,
                            ShoppingListMapper shoppingListMapper,
                            ShoppingItemMapper shoppingItemMapper,
                            TodoTaskMapper todoTaskMapper) {
        this.householdService = householdService;
        this.transactionRecordMapper = transactionRecordMapper;
        this.transactionCategoryMapper = transactionCategoryMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.shoppingListMapper = shoppingListMapper;
        this.shoppingItemMapper = shoppingItemMapper;
        this.todoTaskMapper = todoTaskMapper;
    }

    /**
     * Overview statistics for a space: total income/expense, item counts, alerts.
     */
    public OverviewResponse getOverview(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<TransactionRecord> transactions = transactionRecordMapper.selectList(
                new LambdaQueryWrapper<TransactionRecord>()
                        .eq(TransactionRecord::getHouseholdId, spaceId));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (TransactionRecord t : transactions) {
            if ("income".equals(t.getType())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if ("expense".equals(t.getType())) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        long inventoryCount = inventoryItemMapper.selectCount(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));

        long shoppingListCount = shoppingListMapper.selectCount(
                new LambdaQueryWrapper<ShoppingList>()
                        .eq(ShoppingList::getHouseholdId, spaceId));

        // Count inventory items that are low stock
        List<InventoryItem> allItems = inventoryItemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));
        long alertCount = allItems.stream()
                .filter(item -> item.getLowStockThreshold() != null
                        && item.getQuantity() != null
                        && item.getQuantity().compareTo(item.getLowStockThreshold()) < 0)
                .count();

        return new OverviewResponse(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                transactions.size(),
                inventoryCount,
                shoppingListCount,
                alertCount
        );
    }

    /**
     * Monthly finance summary with category breakdown.
     */
    public FinanceMonthlyResponse getFinanceMonthly(Long userId, Long spaceId, int year, int month) {
        householdService.requireSpaceMembership(userId, spaceId);

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<TransactionRecord> records = transactionRecordMapper.selectList(
                new LambdaQueryWrapper<TransactionRecord>()
                        .eq(TransactionRecord::getHouseholdId, spaceId)
                        .ge(TransactionRecord::getOccurredAt, start)
                        .lt(TransactionRecord::getOccurredAt, end));

        // Build category name lookup
        List<TransactionCategory> allCategories = transactionCategoryMapper.selectList(
                new LambdaQueryWrapper<TransactionCategory>()
                        .eq(TransactionCategory::getHouseholdId, spaceId));
        Map<Long, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(TransactionCategory::getId, TransactionCategory::getName));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        // Group by category for expenses
        Map<Long, List<TransactionRecord>> byCategory = records.stream()
                .filter(t -> "expense".equals(t.getType()))
                .collect(Collectors.groupingBy(TransactionRecord::getCategoryId));

        List<FinanceMonthlyResponse.CategoryBreakdown> categories = new ArrayList<>();
        for (Map.Entry<Long, List<TransactionRecord>> entry : byCategory.entrySet()) {
            Long catId = entry.getKey();
            List<TransactionRecord> catRecords = entry.getValue();
            BigDecimal catTotal = catRecords.stream()
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String catName = catId != null ? categoryNameMap.getOrDefault(catId, "未分类") : "未分类";
            categories.add(new FinanceMonthlyResponse.CategoryBreakdown(catId, catName, catTotal, catRecords.size()));
        }

        for (TransactionRecord t : records) {
            if ("income".equals(t.getType())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if ("expense".equals(t.getType())) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        return new FinanceMonthlyResponse(
                year,
                month,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                categories
        );
    }

    /**
     * Finance categories breakdown: expense and income grouped by category for a given month.
     */
    public FinanceCategoriesResponse getFinanceCategories(Long userId, Long spaceId, int year, int month) {
        householdService.requireSpaceMembership(userId, spaceId);

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<TransactionRecord> records = transactionRecordMapper.selectList(
                new LambdaQueryWrapper<TransactionRecord>()
                        .eq(TransactionRecord::getHouseholdId, spaceId)
                        .ge(TransactionRecord::getOccurredAt, start)
                        .lt(TransactionRecord::getOccurredAt, end));

        List<TransactionCategory> allCategories = transactionCategoryMapper.selectList(
                new LambdaQueryWrapper<TransactionCategory>()
                        .eq(TransactionCategory::getHouseholdId, spaceId));
        Map<Long, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(TransactionCategory::getId, TransactionCategory::getName));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (TransactionRecord t : records) {
            if ("income".equals(t.getType())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if ("expense".equals(t.getType())) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        List<FinanceCategoriesResponse.CategoryDetail> expenseCategories = buildCategoryDetails(
                records, "expense", categoryNameMap);
        List<FinanceCategoriesResponse.CategoryDetail> incomeCategories = buildCategoryDetails(
                records, "income", categoryNameMap);

        return new FinanceCategoriesResponse(year, month, totalIncome, totalExpense,
                expenseCategories, incomeCategories);
    }

    private List<FinanceCategoriesResponse.CategoryDetail> buildCategoryDetails(
            List<TransactionRecord> records, String type, Map<Long, String> categoryNameMap) {
        Map<Long, List<TransactionRecord>> byCategory = records.stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.groupingBy(TransactionRecord::getCategoryId));

        List<FinanceCategoriesResponse.CategoryDetail> details = new ArrayList<>();
        for (Map.Entry<Long, List<TransactionRecord>> entry : byCategory.entrySet()) {
            Long catId = entry.getKey();
            List<TransactionRecord> catRecords = entry.getValue();
            BigDecimal catTotal = catRecords.stream()
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String catName = catId != null ? categoryNameMap.getOrDefault(catId, "未分类") : "未分类";
            details.add(new FinanceCategoriesResponse.CategoryDetail(catId, catName, catTotal, catRecords.size()));
        }
        details.sort((a, b) -> b.amount().compareTo(a.amount()));
        return details;
    }

    /**
     * Shopping statistics: list counts by status, item purchase ratio, 30-day trend.
     */
    public ShoppingStatsResponse getShoppingStats(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<ShoppingList> lists = shoppingListMapper.selectList(
                new LambdaQueryWrapper<ShoppingList>()
                        .eq(ShoppingList::getHouseholdId, spaceId));

        long activeLists = lists.stream()
                .filter(l -> !"completed".equals(l.getStatus()) && !"cancelled".equals(l.getStatus()))
                .count();
        long completedLists = lists.stream()
                .filter(l -> "completed".equals(l.getStatus()))
                .count();

        // Get all shopping items for these lists
        List<Long> listIds = lists.stream().map(ShoppingList::getId).collect(Collectors.toList());
        List<ShoppingItem> allItems = new ArrayList<>();
        if (!listIds.isEmpty()) {
            allItems = shoppingItemMapper.selectList(
                    new LambdaQueryWrapper<ShoppingItem>()
                            .in(ShoppingItem::getShoppingListId, listIds));
        }

        long purchasedItems = allItems.stream()
                .filter(item -> Boolean.TRUE.equals(item.getPurchased()))
                .count();

        // 30-day trend: count lists created per day
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        List<ShoppingList> recentLists = lists.stream()
                .filter(l -> l.getCreatedAt() != null && !l.getCreatedAt().isBefore(thirtyDaysAgo))
                .collect(Collectors.toList());

        Map<String, Long> dailyCounts = recentLists.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()));

        List<ShoppingStatsResponse.DailyTrend> recent30Days = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String date = thirtyDaysAgo.plusDays(i).toLocalDate().toString();
            long count = dailyCounts.getOrDefault(date, 0L);
            recent30Days.add(new ShoppingStatsResponse.DailyTrend(date, count));
        }

        return new ShoppingStatsResponse(
                lists.size(),
                activeLists,
                completedLists,
                allItems.size(),
                purchasedItems,
                recent30Days
        );
    }

    /**
     * Inventory alerts: items expiring within 7 days and low stock items.
     */
    public InventoryAlertsResponse getInventoryAlerts(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<InventoryItem> items = inventoryItemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        List<InventoryAlertsResponse.AlertItem> expiringItems = items.stream()
                .filter(item -> item.getExpireAt() != null
                        && item.getExpireAt().isAfter(now)
                        && item.getExpireAt().isBefore(sevenDaysLater))
                .map(item -> toAlertItem(item, "expiring"))
                .collect(Collectors.toList());

        List<InventoryAlertsResponse.AlertItem> lowStockItems = items.stream()
                .filter(item -> item.getLowStockThreshold() != null
                        && item.getQuantity() != null
                        && item.getQuantity().compareTo(item.getLowStockThreshold()) <= 0)
                .map(item -> toAlertItem(item, "low_stock"))
                .collect(Collectors.toList());

        int totalAlerts = expiringItems.size() + lowStockItems.size();
        return new InventoryAlertsResponse(expiringItems, lowStockItems, totalAlerts);
    }

    private InventoryAlertsResponse.AlertItem toAlertItem(InventoryItem item, String alertType) {
        return new InventoryAlertsResponse.AlertItem(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getQuantity(),
                item.getUnit(),
                item.getLocation(),
                item.getExpireAt(),
                item.getLowStockThreshold(),
                alertType
        );
    }

    /**
     * Inventory statistics: total items, low stock count, breakdown by category.
     */
    public InventoryStatsResponse getInventoryStats(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<InventoryItem> items = inventoryItemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));

        long lowStockCount = items.stream()
                .filter(item -> item.getLowStockThreshold() != null
                        && item.getQuantity() != null
                        && item.getQuantity().compareTo(item.getLowStockThreshold()) < 0)
                .count();

        // Group by category
        Map<String, Long> byCategory = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCategory() != null ? item.getCategory() : "未分类",
                        Collectors.counting()));

        List<InventoryStatsResponse.CategoryCount> categoryCounts = byCategory.entrySet().stream()
                .map(e -> new InventoryStatsResponse.CategoryCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .collect(Collectors.toList());

        return new InventoryStatsResponse(items.size(), lowStockCount, categoryCounts);
    }

    /**
     * Todo task statistics: counts by status, overdue count, completion rate, 30-day completion trend.
     */
    public TodoStatsResponse getTodoStats(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<TodoTask> tasks = todoTaskMapper.selectList(
                new LambdaQueryWrapper<TodoTask>()
                        .eq(TodoTask::getHouseholdId, spaceId));

        long pending = tasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
        long completed = tasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
        long cancelled = tasks.stream().filter(t -> "cancelled".equals(t.getStatus())).count();
        long overdue = tasks.stream().filter(t -> {
            if (!"pending".equals(t.getStatus()) && !"in_progress".equals(t.getStatus())) return false;
            return t.getDueAt() != null && t.getDueAt().isBefore(LocalDateTime.now());
        }).count();

        // Completion rate = completed / (total - cancelled) to exclude cancelled tasks
        long actionable = tasks.size() - cancelled;
        double completionRate = actionable > 0 ? (double) completed / actionable : 0.0;

        // 30-day trend: count tasks completed per day (using updatedAt for completed tasks)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        List<TodoTask> recentCompleted = tasks.stream()
                .filter(t -> "completed".equals(t.getStatus())
                        && t.getUpdatedAt() != null
                        && !t.getUpdatedAt().isBefore(thirtyDaysAgo))
                .collect(Collectors.toList());

        Map<String, Long> dailyCounts = recentCompleted.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getUpdatedAt().toLocalDate().toString(),
                        Collectors.counting()));

        List<TodoStatsResponse.DailyTrend> recent30Days = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String date = thirtyDaysAgo.plusDays(i).toLocalDate().toString();
            long count = dailyCounts.getOrDefault(date, 0L);
            recent30Days.add(new TodoStatsResponse.DailyTrend(date, count));
        }

        return new TodoStatsResponse(tasks.size(), pending, inProgress, completed, cancelled, overdue,
                completionRate, recent30Days);
    }
}
