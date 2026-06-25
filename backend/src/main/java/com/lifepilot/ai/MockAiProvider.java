package com.lifepilot.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.ai.dto.RecipeRecommendationResponse;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.inventory.InventoryItem;
import com.lifepilot.recipe.MealPlan;
import com.lifepilot.recipe.Recipe;

/**
 * Mock AI provider that parses natural language into transaction drafts
 * using simple deterministic rules. Never calls external APIs.
 *
 * Supported patterns (examples):
 *   "午餐花了32元"
 *   "收入工资8000"
 *   "超市购物120.5"
 *   "打车25块"
 *   "咖啡28"
 *
 * Limitations:
 *   - Only supports CNY
 *   - Category inference is keyword-based, not ML-based
 *   - Dates default to now (no date parsing in mock)
 */
public class MockAiProvider implements AiProvider {

    // Pattern: optional_type_hint + optional_merchant + amount + currency_unit
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(?:元|块|¥|￥)?"
    );

    private static final Map<String, String> EXPENSE_KEYWORDS = Map.ofEntries(
            Map.entry("午餐", "餐饮"),
            Map.entry("午饭", "餐饮"),
            Map.entry("晚餐", "餐饮"),
            Map.entry("晚饭", "餐饮"),
            Map.entry("早餐", "餐饮"),
            Map.entry("吃饭", "餐饮"),
            Map.entry("外卖", "餐饮"),
            Map.entry("饭", "餐饮"),
            Map.entry("咖啡", "饮品"),
            Map.entry("奶茶", "饮品"),
            Map.entry("超市", "食品日用"),
            Map.entry("购物", "食品日用"),
            Map.entry("打车", "交通"),
            Map.entry("出租", "交通"),
            Map.entry("地铁", "交通"),
            Map.entry("公交", "交通"),
            Map.entry("加油", "交通"),
            Map.entry("电影", "娱乐"),
            Map.entry("游戏", "娱乐"),
            Map.entry("书", "教育"),
            Map.entry("买书", "教育"),
            Map.entry("医院", "医疗"),
            Map.entry("药", "医疗"),
            Map.entry("房租", "住房"),
            Map.entry("水电", "住房"),
            Map.entry("物业", "住房")
    );

    private static final Map<String, String> INCOME_KEYWORDS = Map.ofEntries(
            Map.entry("工资", "工资"),
            Map.entry("薪资", "工资"),
            Map.entry("奖金", "奖金"),
            Map.entry("红包", "红包"),
            Map.entry("利息", "理财"),
            Map.entry("理财", "理财"),
            Map.entry("退款", "退款"),
            Map.entry("报销", "报销")
    );

    @Override
    public TransactionDraftResponse parseTransaction(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmed = text.trim();

        // Extract amount
        Matcher matcher = AMOUNT_PATTERN.matcher(trimmed);
        BigDecimal amount = null;
        while (matcher.find()) {
            try {
                BigDecimal candidate = new BigDecimal(matcher.group(1));
                if (candidate.compareTo(BigDecimal.ZERO) > 0) {
                    amount = candidate;
                    break; // take first valid positive number
                }
            } catch (NumberFormatException ignored) {
                // skip
            }
        }

        if (amount == null) {
            return new TransactionDraftResponse(
                    null, null, "CNY", null, null, null,
                    null, true, trimmed, "未识别到金额，请手动输入金额。"
            );
        }

        // Determine type and category from keywords
        String type = "expense";
        String category = null;

        // Check income keywords first (more specific)
        for (Map.Entry<String, String> entry : INCOME_KEYWORDS.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                type = "income";
                category = entry.getValue();
                break;
            }
        }

        // If not income, check expense keywords
        if (category == null) {
            for (Map.Entry<String, String> entry : EXPENSE_KEYWORDS.entrySet()) {
                if (trimmed.contains(entry.getKey())) {
                    type = "expense";
                    category = entry.getValue();
                    break;
                }
            }
        }

        // Extract merchant: text before the first digit that looks like the amount
        String merchant = extractMerchant(trimmed, amount);

        // If no merchant found, use the whole text as note
        String note = trimmed;
        boolean needsReview = category == null;

        return new TransactionDraftResponse(
                type,
                amount,
                "CNY",
                LocalDateTime.now(),
                merchant,
                category,
                note,
                needsReview,
                trimmed,
                needsReview ? "未能确定分类，请手动选择。" : null
        );
    }

    // ---- Shopping list parsing ----

    // Pattern: item separator (、, ，, ;, or newline)
    private static final Pattern ITEM_SEPARATOR = Pattern.compile("[、，;；\\n]+");

    // Pattern: quantity + optional unit before item name, e.g. "2斤苹果", "3瓶牛奶"
    private static final Pattern QTY_UNIT_NAME = Pattern.compile(
            "^(\\d+(?:\\.\\d+)?)\\s*(斤|个|包|袋|瓶|盒|箱|根|条|只|块|罐|片|份|kg|g|ml|L)?\\s*(.+)$"
    );

    // Common shopping context keywords to generate a list name
    private static final Map<String, String> SHOPPING_CONTEXT = Map.ofEntries(
            Map.entry("菜", "买菜清单"),
            Map.entry("超市", "超市购物清单"),
            Map.entry("日用", "日用品清单"),
            Map.entry("水果", "水果清单"),
            Map.entry("零食", "零食清单"),
            Map.entry("饮料", "饮品清单"),
            Map.entry("生鲜", "生鲜采购清单"),
            Map.entry("调料", "调料采购清单")
    );

    @Override
    public ShoppingDraftResponse parseShoppingList(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmed = text.trim();

        // Try to extract a list name from context keywords
        String listName = null;
        for (Map.Entry<String, String> entry : SHOPPING_CONTEXT.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                listName = entry.getValue();
                break;
            }
        }

        // If no context match, use a generic name
        if (listName == null) {
            listName = "购物清单";
        }

        // Split by common separators to find items
        String[] rawParts = ITEM_SEPARATOR.split(trimmed);
        List<ShoppingDraftResponse.ShoppingDraftItem> items = new ArrayList<>();
        boolean needsReview = false;

        // Clean up prefixes like "买", "购买", "需要", "去超市" etc. from the whole text
        // Longer alternatives must come before shorter ones to avoid partial matching
        String cleanText = trimmed
                .replaceAll("^(我|我们)?(去超市|去菜场|去市场|需要|购买|要|买|去|到)?\\s*", "")
                .trim();

        // Re-split after cleaning
        String[] parts = ITEM_SEPARATOR.split(cleanText);

        for (String part : parts) {
            String p = part.trim();
            if (p.isEmpty()) continue;

            // Remove leading verbs like "买", "买点", "买些"
            p = p.replaceAll("^(买|购买|要|需要|带|拿|买点|买些|带点|拿点)\\s*", "").trim();
            if (p.isEmpty()) continue;

            // Try to parse quantity+unit+name pattern
            Matcher qtyMatcher = QTY_UNIT_NAME.matcher(p);
            if (qtyMatcher.matches()) {
                try {
                    BigDecimal qty = new BigDecimal(qtyMatcher.group(1));
                    String unit = qtyMatcher.group(2); // may be null
                    String name = qtyMatcher.group(3).trim();
                    if (!name.isEmpty() && qty.compareTo(BigDecimal.ZERO) > 0) {
                        items.add(new ShoppingDraftResponse.ShoppingDraftItem(name, qty, unit, null));
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }

            // Simple case: just an item name
            if (!p.isEmpty()) {
                // Remove trailing particles
                p = p.replaceAll("[等之类的，。,\\.]+$", "").trim();
                if (!p.isEmpty()) {
                    items.add(new ShoppingDraftResponse.ShoppingDraftItem(p, BigDecimal.ONE, null, null));
                }
            }
        }

        if (items.isEmpty()) {
            return new ShoppingDraftResponse(
                    listName, null, List.of(), true, trimmed, "未识别到购物物品，请手动添加。"
            );
        }

        needsReview = items.size() == 1 && items.get(0).name().equals(cleanText);

        return new ShoppingDraftResponse(
                listName, null, items, needsReview, trimmed,
                needsReview ? "物品解析不确定，请检查后确认。" : null
        );
    }

    // ---- Todo parsing ----

    // Pattern: item separator for todos (、，;；\n and also 其次/还有/然后/另外)
    private static final Pattern TODO_SEPARATOR = Pattern.compile("[、，;；\\n]+|(?<=。)|(?:(?=其次|还有|然后|另外))");

    // Priority keywords - use LinkedHashMap to ensure longer keywords are checked first
    private static final Map<String, String> PRIORITY_KEYWORDS = new java.util.LinkedHashMap<>();
    static {
        // Longest keywords first to avoid partial matching (e.g., "急" matching inside "紧急")
        PRIORITY_KEYWORDS.put("紧急", "urgent");
        PRIORITY_KEYWORDS.put("立刻", "urgent");
        PRIORITY_KEYWORDS.put("马上", "urgent");
        PRIORITY_KEYWORDS.put("赶紧", "urgent");
        PRIORITY_KEYWORDS.put("重要", "high");
        PRIORITY_KEYWORDS.put("尽快", "high");
        PRIORITY_KEYWORDS.put("优先", "high");
        PRIORITY_KEYWORDS.put("不急", "low");
        PRIORITY_KEYWORDS.put("有空", "low");
        PRIORITY_KEYWORDS.put("闲了", "low");
        PRIORITY_KEYWORDS.put("随便", "low");
        PRIORITY_KEYWORDS.put("急", "urgent");
    }

    // Due date hint keywords (relative)
    private static final Map<String, Integer> DUE_DAY_HINTS = Map.ofEntries(
            Map.entry("今天", 0),
            Map.entry("今日", 0),
            Map.entry("明天", 1),
            Map.entry("明日", 1),
            Map.entry("后天", 2),
            Map.entry("这周", 5),
            Map.entry("本周", 5),
            Map.entry("下周", 12),
            Map.entry("月底", 0), // special: end of month
            Map.entry("下个月", 30)
    );

    @Override
    public TodoDraftResponse parseTodo(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmed = text.trim();

        // Try to detect due date hint from the whole text
        LocalDateTime detectedDueAt = null;
        String detectedPriority = null;

        for (Map.Entry<String, Integer> entry : DUE_DAY_HINTS.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                if (entry.getValue() == 0 && trimmed.contains("月底")) {
                    // end of month
                    LocalDateTime now = LocalDateTime.now();
                    detectedDueAt = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                            .withHour(23).withMinute(59).withSecond(0).withNano(0);
                } else {
                    detectedDueAt = LocalDateTime.now().plusDays(entry.getValue());
                }
                break;
            }
        }

        for (Map.Entry<String, String> entry : PRIORITY_KEYWORDS.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                detectedPriority = entry.getValue();
                break;
            }
        }

        // Strip common prefixes
        String cleanText = trimmed
                .replaceAll("^(我|我们)?(要|需要|得|必须|应该|计划|打算|安排|记得)?\\s*", "")
                .trim();

        // Split by separators to get multiple items
        String[] parts = TODO_SEPARATOR.split(cleanText);
        List<TodoDraftResponse.TodoDraftItem> items = new ArrayList<>();

        for (String part : parts) {
            String p = part.trim();
            if (p.isEmpty()) continue;

            // Remove leading verbs
            p = p.replaceAll("^(要|需要|得|必须|应该|做|完成|处理|办|去|记得|别忘了)\\s*", "").trim();
            // Remove trailing particles
            p = p.replaceAll("[等之类的，。,\\.]+$", "").trim();
            if (p.isEmpty()) continue;

            // Try to extract per-item priority
            String itemPriority = detectedPriority;
            for (Map.Entry<String, String> entry : PRIORITY_KEYWORDS.entrySet()) {
                if (p.contains(entry.getKey())) {
                    itemPriority = entry.getValue();
                    // Remove the priority keyword from the title
                    p = p.replace(entry.getKey(), "").trim();
                    break;
                }
            }

            // Remove due date hints from individual items to clean the title
            String title = p;
            for (String hint : DUE_DAY_HINTS.keySet()) {
                title = title.replace(hint, "").trim();
            }
            // Remove common trailing chars
            title = title.replaceAll("^[的了]+|[的了]+$", "").trim();

            if (title.isEmpty()) continue;

            items.add(new TodoDraftResponse.TodoDraftItem(
                    title, null, itemPriority, detectedDueAt
            ));
        }

        if (items.isEmpty()) {
            return new TodoDraftResponse(
                    List.of(), true, trimmed, "未识别到待办事项，请手动输入。"
            );
        }

        boolean needsReview = items.size() == 1 && items.get(0).title().length() < 2;

        return new TodoDraftResponse(
                items, needsReview, trimmed,
                needsReview ? "待办内容解析不确定，请检查后确认。" : null
        );
    }

    // ---- Recipe recommendation ----

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> INGREDIENTS_TYPE = new TypeReference<>() {};

    @Override
    public RecipeRecommendationResponse recommendRecipes(List<InventoryItem> inventory, List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return new RecipeRecommendationResponse(List.of());
        }

        // Build inventory name set (lowercased for matching)
        List<String> inventoryNames = inventory.stream()
                .map(InventoryItem::getName)
                .filter(n -> n != null && !n.isBlank())
                .map(n -> n.toLowerCase().trim())
                .collect(Collectors.toList());

        List<RecipeRecommendationResponse.RecommendedRecipe> results = new ArrayList<>();

        for (Recipe recipe : recipes) {
            List<String> ingredientNames = extractIngredientNames(recipe.getIngredientsJson());
            if (ingredientNames.isEmpty()) {
                // Can't evaluate recipes with no ingredients
                results.add(new RecipeRecommendationResponse.RecommendedRecipe(
                        recipe.getId(), recipe.getName(),
                        List.of(), List.of(), 0.0,
                        "该菜谱没有食材信息"
                ));
                continue;
            }

            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            for (String ingredient : ingredientNames) {
                boolean found = false;
                String ingLower = ingredient.toLowerCase().trim();
                for (String invName : inventoryNames) {
                    // Bidirectional contains match
                    if (invName.contains(ingLower) || ingLower.contains(invName)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    matched.add(ingredient);
                } else {
                    missing.add(ingredient);
                }
            }

            double score = (double) matched.size() / ingredientNames.size();
            String reason;
            if (score >= 1.0) {
                reason = "所有食材均可在库存中找到";
            } else if (score >= 0.7) {
                reason = "大部分食材已有，仅缺少" + missing.size() + "种";
            } else if (score >= 0.3) {
                reason = "部分食材可从库存中获取";
            } else {
                reason = "所需食材大部分不在库存中";
            }

            results.add(new RecipeRecommendationResponse.RecommendedRecipe(
                    recipe.getId(), recipe.getName(),
                    matched, missing, Math.round(score * 100.0) / 100.0,
                    reason
            ));
        }

        // Sort by score descending, then by missing count ascending
        results.sort(Comparator
                .comparingDouble(RecipeRecommendationResponse.RecommendedRecipe::matchScore).reversed()
                .thenComparingInt(r -> r.missingIngredients().size()));

        return new RecipeRecommendationResponse(results);
    }

    @Override
    public ShoppingDraftResponse draftShoppingListFromMealPlan(
            List<MealPlan> mealPlans,
            List<Recipe> recipes,
            List<InventoryItem> inventory) {
        if (mealPlans == null || mealPlans.isEmpty()) {
            return new ShoppingDraftResponse(
                    "饮食计划采购清单", null, List.of(), true, null,
                    "当前日期范围内没有饮食计划，请先安排菜谱。"
            );
        }
        if (recipes == null || recipes.isEmpty()) {
            return new ShoppingDraftResponse(
                    "饮食计划采购清单", null, List.of(), true, null,
                    "饮食计划中的菜谱信息不完整，请检查后再生成采购清单。"
            );
        }

        Map<Long, Recipe> recipeById = recipes.stream()
                .filter(recipe -> recipe.getId() != null)
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe, (a, b) -> a));

        Map<String, ShoppingDraftResponse.ShoppingDraftItem> neededByKey = new java.util.LinkedHashMap<>();
        for (MealPlan mealPlan : mealPlans) {
            Recipe recipe = recipeById.get(mealPlan.getRecipeId());
            if (recipe == null) {
                continue;
            }
            for (ShoppingDraftResponse.ShoppingDraftItem item : extractIngredientItems(recipe.getIngredientsJson())) {
                String key = item.name().toLowerCase().trim() + "|" + normalizeUnit(item.unit());
                ShoppingDraftResponse.ShoppingDraftItem existing = neededByKey.get(key);
                if (existing == null) {
                    neededByKey.put(key, item);
                } else {
                    neededByKey.put(key, new ShoppingDraftResponse.ShoppingDraftItem(
                            existing.name(),
                            existing.quantity().add(item.quantity()),
                            existing.unit(),
                            null
                    ));
                }
            }
        }

        if (neededByKey.isEmpty()) {
            return new ShoppingDraftResponse(
                    "饮食计划采购清单", null, List.of(), true,
                    buildMealPlanRawInput(mealPlans, recipeById),
                    "饮食计划中的菜谱缺少食材信息，请补充后再生成采购清单。"
            );
        }

        List<ShoppingDraftResponse.ShoppingDraftItem> missingItems = neededByKey.values().stream()
                .map(item -> subtractInventory(item, inventory != null ? inventory : List.of()))
                .filter(item -> item != null && item.quantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (missingItems.isEmpty()) {
            return new ShoppingDraftResponse(
                    "饮食计划采购清单", null, List.of(), false,
                    buildMealPlanRawInput(mealPlans, recipeById),
                    "当前库存已覆盖所选饮食计划的主要食材。"
            );
        }

        return new ShoppingDraftResponse(
                "饮食计划采购清单", null, missingItems, true,
                buildMealPlanRawInput(mealPlans, recipeById),
                "已根据饮食计划和当前库存生成缺口清单，请确认数量和单位后创建。"
        );
    }

    /**
     * Parse ingredientsJson to extract ingredient names.
     * Expected format: JSON array of objects with "name" field, e.g.
     * [{"name":"鸡蛋","quantity":"2个"}, {"name":"番茄","quantity":"1个"}]
     * Also supports simpler format: ["鸡蛋", "番茄"]
     */
    List<String> extractIngredientNames(String ingredientsJson) {
        if (ingredientsJson == null || ingredientsJson.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = JSON_MAPPER.readValue(ingredientsJson, INGREDIENTS_TYPE);
            return list.stream()
                    .map(map -> {
                        Object name = map.get("name");
                        return name != null ? name.toString().trim() : null;
                    })
                    .filter(n -> n != null && !n.isEmpty())
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            // Try simpler format: plain string array
            try {
                List<String> names = JSON_MAPPER.readValue(ingredientsJson,
                        new TypeReference<List<String>>() {});
                return names.stream()
                        .filter(n -> n != null && !n.isBlank())
                        .map(String::trim)
                        .collect(Collectors.toList());
            } catch (JsonProcessingException e2) {
                return List.of();
            }
        }
    }

    List<ShoppingDraftResponse.ShoppingDraftItem> extractIngredientItems(String ingredientsJson) {
        if (ingredientsJson == null || ingredientsJson.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = JSON_MAPPER.readValue(ingredientsJson, INGREDIENTS_TYPE);
            return list.stream()
                    .map(this::toIngredientItem)
                    .filter(item -> item != null && item.name() != null && !item.name().isBlank())
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            try {
                List<String> names = JSON_MAPPER.readValue(ingredientsJson,
                        new TypeReference<List<String>>() {});
                return names.stream()
                        .filter(n -> n != null && !n.isBlank())
                        .map(name -> new ShoppingDraftResponse.ShoppingDraftItem(name.trim(), BigDecimal.ONE, null, null))
                        .collect(Collectors.toList());
            } catch (JsonProcessingException e2) {
                return List.of();
            }
        }
    }

    private ShoppingDraftResponse.ShoppingDraftItem toIngredientItem(Map<String, Object> map) {
        Object rawName = map.get("name");
        if (rawName == null || rawName.toString().isBlank()) {
            return null;
        }
        String name = rawName.toString().trim();
        String unit = valueAsString(map.get("unit"));
        BigDecimal quantity = null;
        Object rawQuantity = map.get("quantity");
        if (rawQuantity instanceof Number number) {
            quantity = new BigDecimal(number.toString());
        } else {
            QuantityParts parts = parseQuantityParts(valueAsString(rawQuantity));
            quantity = parts.quantity();
            if (unit == null) {
                unit = parts.unit();
            }
        }
        if (quantity == null) {
            quantity = BigDecimal.ONE;
        }

        return new ShoppingDraftResponse.ShoppingDraftItem(name, quantity, unit, null);
    }

    private ShoppingDraftResponse.ShoppingDraftItem subtractInventory(
            ShoppingDraftResponse.ShoppingDraftItem needed,
            List<InventoryItem> inventory) {
        for (InventoryItem item : inventory) {
            if (item.getName() == null || item.getName().isBlank()) {
                continue;
            }
            String inventoryName = item.getName().toLowerCase().trim();
            String neededName = needed.name().toLowerCase().trim();
            if (!inventoryName.contains(neededName) && !neededName.contains(inventoryName)) {
                continue;
            }

            BigDecimal inventoryQty = item.getQuantity();
            if (inventoryQty == null || inventoryQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String inventoryUnit = normalizeUnit(item.getUnit());
            String neededUnit = normalizeUnit(needed.unit());
            if (inventoryUnit.equals(neededUnit) && !neededUnit.isBlank()) {
                BigDecimal shortage = needed.quantity().subtract(inventoryQty);
                if (shortage.compareTo(BigDecimal.ZERO) > 0) {
                    return new ShoppingDraftResponse.ShoppingDraftItem(needed.name(), shortage, needed.unit(), null);
                }
            }
            return null;
        }
        return needed;
    }

    private String buildMealPlanRawInput(List<MealPlan> mealPlans, Map<Long, Recipe> recipeById) {
        return mealPlans.stream()
                .map(plan -> {
                    Recipe recipe = recipeById.get(plan.getRecipeId());
                    String recipeName = recipe != null ? recipe.getName() : "未知菜谱";
                    return plan.getPlannedDate() + " " + plan.getMealType() + " " + recipeName;
                })
                .collect(Collectors.joining("；"));
    }

    private QuantityParts parseQuantityParts(String raw) {
        if (raw == null || raw.isBlank()) {
            return new QuantityParts(null, null);
        }
        Matcher matcher = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([^\\d\\s]+)?$").matcher(raw.trim());
        if (!matcher.matches()) {
            return new QuantityParts(null, null);
        }
        return new QuantityParts(new BigDecimal(matcher.group(1)), matcher.group(2));
    }

    private String valueAsString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeUnit(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase();
    }

    private record QuantityParts(BigDecimal quantity, String unit) {
    }

    /**
     * Try to extract merchant name from text. Looks for text segments before
     * the amount number, filtering out common stop words.
     */
    private String extractMerchant(String text, BigDecimal amount) {
        String amountStr = amount.stripTrailingZeros().toPlainString();

        // Find where the amount starts in the text
        int amountIdx = -1;
        for (int i = 0; i <= text.length() - amountStr.length(); i++) {
            if (text.substring(i).startsWith(amountStr)) {
                amountIdx = i;
                break;
            }
        }

        if (amountIdx <= 0) {
            return null;
        }

        // Get text before the amount
        String beforeAmount = text.substring(0, amountIdx).trim();

        // Remove common verbs and particles
        beforeAmount = beforeAmount
                .replaceAll("^(今天|昨晚|刚才|上午|下午|晚上)?", "")
                .replaceAll("(花|花了|消费|支出|付|付款|买了|买|收入|收到|工资|赚了?)", "")
                .replaceAll("[了的]", "")
                .trim();

        if (beforeAmount.isEmpty()) {
            return null;
        }

        return beforeAmount;
    }
}
