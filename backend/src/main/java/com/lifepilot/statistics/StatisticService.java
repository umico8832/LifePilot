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
import com.lifepilot.statistics.dto.FinanceMonthlyResponse;
import com.lifepilot.statistics.dto.OverviewResponse;

@Service
public class StatisticService {

    private final HouseholdService householdService;
    private final TransactionRecordMapper transactionRecordMapper;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingItemMapper shoppingItemMapper;

    public StatisticService(HouseholdService householdService,
                            TransactionRecordMapper transactionRecordMapper,
                            TransactionCategoryMapper transactionCategoryMapper,
                            InventoryItemMapper inventoryItemMapper,
                            ShoppingListMapper shoppingListMapper,
                            ShoppingItemMapper shoppingItemMapper) {
        this.householdService = householdService;
        this.transactionRecordMapper = transactionRecordMapper;
        this.transactionCategoryMapper = transactionCategoryMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.shoppingListMapper = shoppingListMapper;
        this.shoppingItemMapper = shoppingItemMapper;
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
}