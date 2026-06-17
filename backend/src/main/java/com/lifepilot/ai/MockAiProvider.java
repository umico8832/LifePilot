package com.lifepilot.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

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