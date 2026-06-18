package com.lifepilot.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;

/**
 * Unit tests for {@link OpenAiProvider} using a mock HTTP server.
 *
 * <p>These tests verify JSON deserialization, retry logic, error handling,
 * and code-fence stripping without calling any real API.</p>
 */
class OpenAiProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private OpenAiProvider provider;
    private com.sun.net.httpserver.HttpServer mockServer;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        // Start a lightweight HTTP server for each test
        mockServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(0), 0);
        port = mockServer.getAddress().getPort();
        mockServer.start();
    }

    private void stopServer() {
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    private OpenAiProvider createProvider() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .requestFactory(factory)
                .build();
        return new OpenAiProvider(restClient, "test-model", 0.2, 512, 2, objectMapper);
    }

    @Test
    void parseTransaction_success() throws Exception {
        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "content": "{\\"type\\":\\"expense\\",\\"amount\\":32.00,\\"currency\\":\\"CNY\\",\\"occurredAt\\":null,\\"merchant\\":\\"咖啡店\\",\\"categoryName\\":\\"饮品\\",\\"note\\":\\"咖啡\\",\\"needsReview\\":false,\\"rawInput\\":\\"咖啡32元\\",\\"validationMessage\\":null}"
                    }
                  }]
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TransactionDraftResponse result = provider.parseTransaction("咖啡32元");
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo("expense");
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("32.00"));
            assertThat(result.currency()).isEqualTo("CNY");
            assertThat(result.merchant()).isEqualTo("咖啡店");
            assertThat(result.categoryName()).isEqualTo("饮品");
            assertThat(result.needsReview()).isFalse();
        } finally {
            stopServer();
        }
    }

    @Test
    void parseShoppingList_success() throws Exception {
        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "content": "{\\"listName\\":\\"超市购物清单\\",\\"estimatedBudget\\":null,\\"items\\":[{\\"name\\":\\"苹果\\",\\"quantity\\":2,\\"unit\\":\\"斤\\",\\"estimatedPrice\\":null},{\\"name\\":\\"牛奶\\",\\"quantity\\":3,\\"unit\\":\\"瓶\\",\\"estimatedPrice\\":null}],\\"needsReview\\":false,\\"rawInput\\":\\"2斤苹果、3瓶牛奶\\",\\"validationMessage\\":null}"
                    }
                  }]
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            ShoppingDraftResponse result = provider.parseShoppingList("2斤苹果、3瓶牛奶");
            assertThat(result).isNotNull();
            assertThat(result.listName()).isEqualTo("超市购物清单");
            assertThat(result.items()).hasSize(2);
            assertThat(result.items().get(0).name()).isEqualTo("苹果");
            assertThat(result.items().get(1).name()).isEqualTo("牛奶");
        } finally {
            stopServer();
        }
    }

    @Test
    void parseTodo_success() throws Exception {
        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "content": "{\\"items\\":[{\\"title\\":\\"买菜\\",\\"description\\":null,\\"priority\\":\\"high\\",\\"dueAt\\":null}],\\"needsReview\\":false,\\"rawInput\\":\\"今天需要买菜\\",\\"validationMessage\\":null}"
                    }
                  }]
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TodoDraftResponse result = provider.parseTodo("今天需要买菜");
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).title()).isEqualTo("买菜");
            assertThat(result.items().get(0).priority()).isEqualTo("high");
        } finally {
            stopServer();
        }
    }

    @Test
    void parseTransaction_stripsCodeFences() throws Exception {
        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "content": "```json\\n{\\"type\\":\\"expense\\",\\"amount\\":10.0,\\"currency\\":\\"CNY\\",\\"occurredAt\\":null,\\"merchant\\":null,\\"categoryName\\":\\"饮品\\",\\"note\\":\\"奶茶\\",\\"needsReview\\":false,\\"rawInput\\":\\"奶茶10\\",\\"validationMessage\\":null}\\n```"
                    }
                  }]
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TransactionDraftResponse result = provider.parseTransaction("奶茶10");
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo("expense");
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("10.0"));
        } finally {
            stopServer();
        }
    }

    @Test
    void parseTransaction_apiReturns500_returnsNull() {
        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = "Internal Server Error".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(500, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TransactionDraftResponse result = provider.parseTransaction("咖啡32元");
            assertThat(result).isNull();
        } finally {
            stopServer();
        }
    }

    @Test
    void parseTransaction_malformedJson_returnsNull() {
        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "content": "this is not json"
                    }
                  }]
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TransactionDraftResponse result = provider.parseTransaction("test");
            assertThat(result).isNull();
        } finally {
            stopServer();
        }
    }

    @Test
    void parseTransaction_emptyChoices_returnsNull() {
        String responseJson = """
                {
                  "choices": []
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TransactionDraftResponse result = provider.parseTransaction("test");
            assertThat(result).isNull();
        } finally {
            stopServer();
        }
    }

    @Test
    void parseTransaction_nullInput_stillCallsApi() throws Exception {
        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "content": "{\\"type\\":\\"expense\\",\\"amount\\":0,\\"currency\\":\\"CNY\\",\\"occurredAt\\":null,\\"merchant\\":null,\\"categoryName\\":null,\\"note\\":\\"\\",\\"needsReview\\":true,\\"rawInput\\":\\"\\",\\"validationMessage\\":\\"无法解析\\"}"
                    }
                  }]
                }
                """;

        mockServer.createContext("/chat/completions", exchange -> {
            byte[] body = responseJson.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        provider = createProvider();
        try {
            TransactionDraftResponse result = provider.parseTransaction(null);
            assertThat(result).isNotNull();
            assertThat(result.needsReview()).isTrue();
        } finally {
            stopServer();
        }
    }
}