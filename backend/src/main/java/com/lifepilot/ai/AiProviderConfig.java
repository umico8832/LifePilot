package com.lifepilot.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Conditionally creates the {@link AiProvider} bean based on
 * {@code lifepilot.ai.provider}.
 *
 * <ul>
 *   <li>{@code mock} (default) – {@link MockAiProvider}</li>
 *   <li>{@code openai} – {@link OpenAiProvider}, falls back to
 *       {@link MockAiProvider} when the API key is blank</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiProviderConfig {

    private static final Logger log = LoggerFactory.getLogger(AiProviderConfig.class);

    @Bean
    public AiProvider aiProvider(AiProviderProperties properties, ObjectMapper objectMapper) {
        String provider = properties.getProvider();

        return switch (provider) {
            case "mock" -> {
                log.info("AI provider: mock (deterministic local parsing)");
                yield new MockAiProvider();
            }
            case "openai" -> {
                AiProviderProperties.OpenAi openai = properties.getOpenai();
                if (openai.getApiKey() == null || openai.getApiKey().isBlank()) {
                    log.warn("AI provider configured as 'openai' but OPENAI_API_KEY is empty – "
                            + "falling back to MockAiProvider");
                    yield new MockAiProvider();
                }
                log.info("AI provider: openai (model={}, baseUrl={})",
                        openai.getModel(), openai.getBaseUrl());
                RestClient restClient = buildRestClient(openai);
                yield new OpenAiProvider(
                        restClient,
                        openai.getModel(),
                        openai.getTemperature(),
                        openai.getMaxTokens(),
                        openai.getRetryMaxAttempts(),
                        objectMapper
                );
            }
            default -> throw new IllegalStateException(
                    "Unknown AI provider '%s'. Supported values: mock, openai"
                            .formatted(provider));
        };
    }

    private RestClient buildRestClient(AiProviderProperties.OpenAi openai) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.toIntExact(openai.getTimeoutSeconds() * 1000L));
        factory.setReadTimeout(Math.toIntExact(openai.getTimeoutSeconds() * 1000L));
        return RestClient.builder()
                .baseUrl(openai.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + openai.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}