package com.lifepilot.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private long spaceId;

    @BeforeEach
    void setup() throws Exception {
        String body = """
                {
                  "email": "ai_%d@example.com",
                  "password": "strong-pass-123",
                  "displayName": "AiUser"
                }
                """.formatted(System.nanoTime());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        token = json.at("/data/accessToken").asText();

        // Get the auto-created personal space
        MvcResult listResult = mockMvc.perform(get("/api/spaces")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        spaceId = listJson.at("/data/0/id").asLong();
    }

    @Test
    void parseExpenseWithKnownCategory() throws Exception {
        String body = """
                { "text": "午餐花了32元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(32))
                .andExpect(jsonPath("$.data.currency").value("CNY"))
                .andExpect(jsonPath("$.data.categoryName").value("餐饮"))
                .andExpect(jsonPath("$.data.needsReview").value(false))
                .andExpect(jsonPath("$.data.rawInput").value("午餐花了32元"));
    }

    @Test
    void parseIncomeWithKnownCategory() throws Exception {
        String body = """
                { "text": "收到工资8000元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("income"))
                .andExpect(jsonPath("$.data.amount").value(8000))
                .andExpect(jsonPath("$.data.categoryName").value("工资"))
                .andExpect(jsonPath("$.data.needsReview").value(false));
    }

    @Test
    void parseAmbiguousInputNeedsReview() throws Exception {
        String body = """
                { "text": "花了50块" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(50))
                .andExpect(jsonPath("$.data.needsReview").value(true))
                .andExpect(jsonPath("$.data.validationMessage").exists());
    }

    @Test
    void parseInputWithNoAmountReturnsDraftWithError() throws Exception {
        String body = """
                { "text": "买了一些东西" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").doesNotExist())
                .andExpect(jsonPath("$.data.needsReview").value(true))
                .andExpect(jsonPath("$.data.validationMessage").value("未识别到金额，请手动输入金额。"));
    }

    @Test
    void parseEmptyTextReturnsValidationError() throws Exception {
        String body = """
                { "text": "" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void parseRequiresAuthentication() throws Exception {
        String body = """
                { "text": "午餐32元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void parseShoppingListWithMultipleItems() throws Exception {
        String body = """
                { "text": "买苹果、牛奶、面包、鸡蛋" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-shopping")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.listName").value("购物清单"))
                .andExpect(jsonPath("$.data.items.length()").value(4))
                .andExpect(jsonPath("$.data.items[0].name").value("苹果"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(1))
                .andExpect(jsonPath("$.data.items[1].name").value("牛奶"))
                .andExpect(jsonPath("$.data.items[2].name").value("面包"))
                .andExpect(jsonPath("$.data.items[3].name").value("鸡蛋"))
                .andExpect(jsonPath("$.data.rawInput").exists());
    }

    @Test
    void parseShoppingListWithContextKeyword() throws Exception {
        String body = """
                { "text": "去超市买2斤苹果、3瓶牛奶、一包纸巾" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-shopping")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.listName").value("超市购物清单"))
                .andExpect(jsonPath("$.data.items[0].name").value("苹果"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].unit").value("斤"))
                .andExpect(jsonPath("$.data.items[1].name").value("牛奶"))
                .andExpect(jsonPath("$.data.items[1].quantity").value(3))
                .andExpect(jsonPath("$.data.items[1].unit").value("瓶"));
    }

    @Test
    void parseShoppingListWithFruitContext() throws Exception {
        String body = """
                { "text": "水果：苹果、香蕉、橙子" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-shopping")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.listName").value("水果清单"))
                .andExpect(jsonPath("$.data.items.length()").value(3));
    }

    @Test
    void parseShoppingListEmptyTextReturns400() throws Exception {
        String body = """
                { "text": "" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-shopping")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void parseShoppingListRequiresAuth() throws Exception {
        String body = """
                { "text": "买苹果" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-shopping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void parseShoppingListSingleItemNeedsReview() throws Exception {
        String body = """
                { "text": "随便买点东西" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-shopping")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.needsReview").value(true))
                .andExpect(jsonPath("$.data.validationMessage").exists());
    }

    @Test
    void parseTodoMultipleItems() throws Exception {
        String body = """
                { "text": "买菜、打扫房间、交水电费" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.items[0].title").value("买菜"))
                .andExpect(jsonPath("$.data.items[1].title").value("打扫房间"))
                .andExpect(jsonPath("$.data.items[2].title").value("交水电费"))
                .andExpect(jsonPath("$.data.rawInput").exists());
    }

    @Test
    void parseTodoWithPriorityKeyword() throws Exception {
        String body = """
                { "text": "紧急处理客户投诉" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("处理客户投诉"))
                .andExpect(jsonPath("$.data.items[0].priority").value("urgent"));
    }

    @Test
    void parseTodoWithDueDateHint() throws Exception {
        String body = """
                { "text": "明天去医院做体检" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("去医院做体检"))
                .andExpect(jsonPath("$.data.items[0].dueAt").exists());
    }

    @Test
    void parseTodoEmptyTextReturns400() throws Exception {
        String body = """
                { "text": "" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void parseTodoRequiresAuth() throws Exception {
        String body = """
                { "text": "买菜" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void parseTodoWithLowPriority() throws Exception {
        String body = """
                { "text": "有空整理书架" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("整理书架"))
                .andExpect(jsonPath("$.data.items[0].priority").value("low"));
    }

    @Test
    void parseDecimalAmount() throws Exception {
        String body = """
                { "text": "超市购物120.50元" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(120.50))
                .andExpect(jsonPath("$.data.categoryName").value("食品日用"));
    }

    @Test
    void parseTransportCategory() throws Exception {
        String body = """
                { "text": "打车25块" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-transaction")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.amount").value(25))
                .andExpect(jsonPath("$.data.categoryName").value("交通"));
    }

    @Test
    void monthlyReportReturnsEmptyData() throws Exception {
        int year = java.time.Year.now().getValue();
        int month = java.time.LocalDate.now().getMonthValue();

        mockMvc.perform(get("/api/ai/spaces/" + spaceId + "/monthly-report")
                        .header("Authorization", "Bearer " + token)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(year))
                .andExpect(jsonPath("$.data.month").value(month))
                .andExpect(jsonPath("$.data.finance.totalIncome").value(0))
                .andExpect(jsonPath("$.data.finance.totalExpense").value(0))
                .andExpect(jsonPath("$.data.finance.balance").value(0))
                .andExpect(jsonPath("$.data.inventory.totalItems").value(0))
                .andExpect(jsonPath("$.data.shopping.listCount").value(0))
                .andExpect(jsonPath("$.data.todo.totalCount").value(0))
                .andExpect(jsonPath("$.data.reportText").isNotEmpty())
                .andExpect(jsonPath("$.data.suggestions").isArray());
    }

    @Test
    void monthlyReportRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/ai/spaces/" + spaceId + "/monthly-report")
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isForbidden());
    }

    @Test
    void callLogsReturnsSanitizedAiAuditRecords() throws Exception {
        String body = """
                { "text": "明天买牛奶" }
                """;

        mockMvc.perform(post("/api/ai/spaces/" + spaceId + "/parse-todo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/ai/spaces/" + spaceId + "/call-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("scenario", "parse_todo")
                        .param("status", "success")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].scenario").value("parse_todo"))
                .andExpect(jsonPath("$.data[0].status").value("success"))
                .andExpect(jsonPath("$.data[0].promptHash").isNotEmpty())
                .andExpect(jsonPath("$.data[0].requestJson").value(org.hamcrest.Matchers.containsString("inputLength")))
                .andExpect(jsonPath("$.data[0].requestJson").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("买牛奶"))));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder get(String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url);
    }
}
