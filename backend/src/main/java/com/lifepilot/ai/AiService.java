package com.lifepilot.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.ai.dto.MonthlyReportResponse;
import com.lifepilot.ai.dto.ParseShoppingRequest;
import com.lifepilot.ai.dto.ParseTodoRequest;
import com.lifepilot.ai.dto.ParseTransactionRequest;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.finance.TransactionCategory;
import com.lifepilot.finance.TransactionCategoryMapper;
import com.lifepilot.finance.TransactionRecord;
import com.lifepilot.finance.TransactionRecordMapper;
import com.lifepilot.ai.dto.RecipeRecommendationResponse;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.inventory.InventoryItemMapper;
import com.lifepilot.recipe.MealPlan;
import com.lifepilot.recipe.MealPlanMapper;
import com.lifepilot.recipe.Recipe;
import com.lifepilot.recipe.RecipeMapper;
import com.lifepilot.shopping.ShoppingList;
import com.lifepilot.shopping.ShoppingListMapper;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.todo.TodoTask;
import com.lifepilot.todo.TodoTaskMapper;

@Service
public class AiService {

    private final AiProvider aiProvider;
    private final HouseholdService householdService;
    private final TransactionRecordMapper transactionRecordMapper;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final ShoppingListMapper shoppingListMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final RecipeMapper recipeMapper;
    private final MealPlanMapper mealPlanMapper;

    public AiService(AiProvider aiProvider, HouseholdService householdService,
                     TransactionRecordMapper transactionRecordMapper,
                     TransactionCategoryMapper transactionCategoryMapper,
                     InventoryItemMapper inventoryItemMapper,
                     ShoppingListMapper shoppingListMapper,
                     TodoTaskMapper todoTaskMapper,
                     RecipeMapper recipeMapper,
                     MealPlanMapper mealPlanMapper) {
        this.aiProvider = aiProvider;
        this.householdService = householdService;
        this.transactionRecordMapper = transactionRecordMapper;
        this.transactionCategoryMapper = transactionCategoryMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.shoppingListMapper = shoppingListMapper;
        this.todoTaskMapper = todoTaskMapper;
        this.recipeMapper = recipeMapper;
        this.mealPlanMapper = mealPlanMapper;
    }

    public TransactionDraftResponse parseTransaction(Long userId, Long spaceId, ParseTransactionRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);
        TransactionDraftResponse draft = aiProvider.parseTransaction(request.text());
        if (draft == null) {
            return new TransactionDraftResponse(
                    null, null, "CNY", null, null, null,
                    null, true, request.text(), "无法解析输入文本，请尝试重新描述。"
            );
        }
        return draft;
    }

    public ShoppingDraftResponse parseShoppingList(Long userId, Long spaceId, ParseShoppingRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);
        ShoppingDraftResponse draft = aiProvider.parseShoppingList(request.text());
        if (draft == null) {
            return new ShoppingDraftResponse(
                    "购物清单", null, java.util.List.of(),
                    true, request.text(), "无法解析输入文本，请尝试重新描述。"
            );
        }
        return draft;
    }

    public TodoDraftResponse parseTodo(Long userId, Long spaceId, ParseTodoRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);
        TodoDraftResponse draft = aiProvider.parseTodo(request.text());
        if (draft == null) {
            return new TodoDraftResponse(
                    java.util.List.of(), true, request.text(), "无法解析输入文本，请尝试重新描述。"
            );
        }
        return draft;
    }

    public MonthlyReportResponse generateMonthlyReport(Long userId, Long spaceId, int year, int month) {
        householdService.requireSpaceMembership(userId, spaceId);

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        // ---- Finance ----
        List<TransactionRecord> records = transactionRecordMapper.selectList(
                new LambdaQueryWrapper<TransactionRecord>()
                        .eq(TransactionRecord::getHouseholdId, spaceId)
                        .ge(TransactionRecord::getOccurredAt, start)
                        .lt(TransactionRecord::getOccurredAt, end));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<Long, List<TransactionRecord>> expenseByCategory = records.stream()
                .filter(t -> "expense".equals(t.getType()))
                .collect(Collectors.groupingBy(TransactionRecord::getCategoryId));

        List<TransactionCategory> allCategories = transactionCategoryMapper.selectList(
                new LambdaQueryWrapper<TransactionCategory>()
                        .eq(TransactionCategory::getHouseholdId, spaceId));
        Map<Long, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(TransactionCategory::getId, TransactionCategory::getName));

        List<MonthlyReportResponse.CategoryItem> topCategories = new ArrayList<>();
        for (Map.Entry<Long, List<TransactionRecord>> entry : expenseByCategory.entrySet()) {
            Long catId = entry.getKey();
            List<TransactionRecord> catRecords = entry.getValue();
            BigDecimal catTotal = catRecords.stream()
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String catName = catId != null ? categoryNameMap.getOrDefault(catId, "未分类") : "未分类";
            topCategories.add(new MonthlyReportResponse.CategoryItem(catName, catTotal, catRecords.size()));
        }
        topCategories.sort((a, b) -> b.amount().compareTo(a.amount()));

        for (TransactionRecord t : records) {
            if ("income".equals(t.getType())) totalIncome = totalIncome.add(t.getAmount());
            else if ("expense".equals(t.getType())) totalExpense = totalExpense.add(t.getAmount());
        }

        MonthlyReportResponse.FinanceSummary finance = new MonthlyReportResponse.FinanceSummary(
                totalIncome, totalExpense, totalIncome.subtract(totalExpense),
                records.size(), topCategories
        );

        // ---- Inventory ----
        List<InventoryItem> inventoryItems = inventoryItemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));
        long lowStockCount = inventoryItems.stream()
                .filter(item -> item.getLowStockThreshold() != null
                        && item.getQuantity() != null
                        && item.getQuantity().compareTo(item.getLowStockThreshold()) < 0)
                .count();
        MonthlyReportResponse.InventorySummary inventory = new MonthlyReportResponse.InventorySummary(
                inventoryItems.size(), lowStockCount
        );

        // ---- Shopping ----
        long shoppingListCount = shoppingListMapper.selectCount(
                new LambdaQueryWrapper<ShoppingList>()
                        .eq(ShoppingList::getHouseholdId, spaceId));
        MonthlyReportResponse.ShoppingSummary shopping = new MonthlyReportResponse.ShoppingSummary(shoppingListCount);

        // ---- Todo ----
        List<TodoTask> todoTasks = todoTaskMapper.selectList(
                new LambdaQueryWrapper<TodoTask>()
                        .eq(TodoTask::getHouseholdId, spaceId));
        long pending = todoTasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
        long completed = todoTasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
        long overdue = todoTasks.stream().filter(t -> {
            if (!"pending".equals(t.getStatus())) return false;
            return t.getDueAt() != null && t.getDueAt().isBefore(LocalDateTime.now());
        }).count();
        MonthlyReportResponse.TodoSummary todo = new MonthlyReportResponse.TodoSummary(
                todoTasks.size(), pending, completed, overdue
        );

        // ---- Highlights and suggestions (mock AI formatting) ----
        List<String> highlights = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (totalIncome.compareTo(totalExpense) > 0) {
            highlights.add("本月结余为正，财务状况良好");
        } else if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            highlights.add("本月支出超过收入，需关注消费");
        }

        if (!topCategories.isEmpty()) {
            highlights.add("最大支出类别为「" + topCategories.get(0).name() + "」，共" + topCategories.get(0).count() + "笔");
        }

        if (lowStockCount > 0) {
            highlights.add(lowStockCount + "件库存物品低于警戒线");
            suggestions.add("建议补充低库存物品");
        }

        if (overdue > 0) {
            highlights.add(overdue + "个待办任务已逾期");
            suggestions.add("建议优先处理逾期任务");
        }

        if (pending > 5) {
            suggestions.add("待处理任务较多，建议适当分批完成");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("各项指标正常，继续保持");
        }

        // Generate report text
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("年").append(month).append("月生活报告\n\n");
        sb.append("【财务概览】\n");
        sb.append("收入：¥").append(totalIncome).append("，支出：¥").append(totalExpense);
        sb.append("，结余：¥").append(totalIncome.subtract(totalExpense));
        sb.append("，共").append(records.size()).append("笔交易\n");
        if (!topCategories.isEmpty()) {
            sb.append("主要支出：");
            topCategories.stream().limit(3).forEach(c ->
                    sb.append(c.name()).append("(¥").append(c.amount()).append(") "));
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("【库存状态】共").append(inventoryItems.size()).append("件");
        if (lowStockCount > 0) sb.append("，").append(lowStockCount).append("件低库存预警");
        sb.append("\n\n");
        sb.append("【购物清单】共").append(shoppingListCount).append("个清单\n\n");
        sb.append("【待办任务】共").append(todoTasks.size()).append("项，待处理").append(pending);
        sb.append("项，已完成").append(completed).append("项");
        if (overdue > 0) sb.append("，逾期").append(overdue).append("项");
        sb.append("\n");

        return new MonthlyReportResponse(
                year, month, finance, inventory, shopping, todo,
                highlights, suggestions, sb.toString()
        );
    }

    public RecipeRecommendationResponse recommendRecipes(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        // Query all inventory items in this space
        List<InventoryItem> inventoryItems = inventoryItemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));

        // Query all recipes in this space
        List<Recipe> recipes = recipeMapper.selectList(
                new LambdaQueryWrapper<Recipe>()
                        .eq(Recipe::getHouseholdId, spaceId));

        return aiProvider.recommendRecipes(inventoryItems, recipes);
    }

    public ShoppingDraftResponse draftShoppingListFromMealPlan(
            Long userId, Long spaceId, LocalDate startDate, LocalDate endDate) {
        householdService.requireSpaceMembership(userId, spaceId);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("VALIDATION_ERROR", "endDate must not be before startDate");
        }

        LambdaQueryWrapper<MealPlan> mealPlanWrapper = new LambdaQueryWrapper<MealPlan>()
                .eq(MealPlan::getHouseholdId, spaceId);
        if (startDate != null) {
            mealPlanWrapper.ge(MealPlan::getPlannedDate, startDate);
        }
        if (endDate != null) {
            mealPlanWrapper.le(MealPlan::getPlannedDate, endDate);
        }
        mealPlanWrapper.orderByAsc(MealPlan::getPlannedDate).orderByAsc(MealPlan::getMealType);
        List<MealPlan> mealPlans = mealPlanMapper.selectList(mealPlanWrapper);

        Set<Long> recipeIds = mealPlans.stream()
                .map(MealPlan::getRecipeId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        List<Recipe> recipes = recipeIds.isEmpty()
                ? List.of()
                : recipeMapper.selectList(new LambdaQueryWrapper<Recipe>()
                        .eq(Recipe::getHouseholdId, spaceId)
                        .in(Recipe::getId, recipeIds));

        List<InventoryItem> inventoryItems = inventoryItemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId));

        return aiProvider.draftShoppingListFromMealPlan(mealPlans, recipes, inventoryItems);
    }
}
