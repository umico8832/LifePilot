package com.lifepilot.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;

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
@Component
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