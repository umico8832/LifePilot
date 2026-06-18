package com.lifepilot.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link AiProviderConfig} to verify conditional bean creation.
 *
 * <p>The default test profile uses {@code provider=mock}, so the injected
 * {@link AiProvider} should always be a {@link MockAiProvider}.</p>
 */
@SpringBootTest
class AiProviderConfigTest {

    @Autowired
    private AiProvider aiProvider;

    @Test
    void defaultProvider_isMock() {
        assertThat(aiProvider).isInstanceOf(MockAiProvider.class);
    }

    /**
     * Directly test that provider=openai with blank API key falls back to MockAiProvider.
     */
    @Test
    void openaiProvider_blankApiKey_fallsBackToMock() {
        AiProviderProperties props = new AiProviderProperties();
        props.setProvider("openai");
        props.getOpenai().setApiKey("");

        AiProviderConfig config = new AiProviderConfig();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AiProvider result = config.aiProvider(props, objectMapper);

        assertThat(result).isInstanceOf(MockAiProvider.class);
    }

    /**
     * Directly test that an unknown provider value throws IllegalStateException.
     */
    @Test
    void unknownProvider_throwsException() {
        AiProviderProperties props = new AiProviderProperties();
        props.setProvider("unknown-provider");

        AiProviderConfig config = new AiProviderConfig();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        assertThatThrownBy(() -> config.aiProvider(props, objectMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown AI provider")
                .hasMessageContaining("unknown-provider");
    }

    /**
     * Directly test that provider=openai with a non-blank API key creates OpenAiProvider.
     */
    @Test
    void openaiProvider_withApiKey_createsOpenAiProvider() {
        AiProviderProperties props = new AiProviderProperties();
        props.setProvider("openai");
        props.getOpenai().setApiKey("sk-test-fake-key");
        props.getOpenai().setBaseUrl("http://localhost:1"); // won't be called

        AiProviderConfig config = new AiProviderConfig();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AiProvider result = config.aiProvider(props, objectMapper);

        assertThat(result).isInstanceOf(OpenAiProvider.class);
    }

    /**
     * Directly test that provider=mock creates MockAiProvider.
     */
    @Test
    void mockProvider_createsMockAiProvider() {
        AiProviderProperties props = new AiProviderProperties();
        props.setProvider("mock");

        AiProviderConfig config = new AiProviderConfig();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AiProvider result = config.aiProvider(props, objectMapper);

        assertThat(result).isInstanceOf(MockAiProvider.class);
    }
}