package com.lifepilot.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AI provider.
 *
 * <pre>
 * lifepilot:
 *   ai:
 *     provider: mock | openai
 *     openai:
 *       api-key: (env OPENAI_API_KEY)
 *       base-url: https://api.openai.com/v1
 *       model: gpt-4o-mini
 *       temperature: 0.2
 *       max-tokens: 1024
 *       timeout-seconds: 30
 *       retry-max-attempts: 2
 * </pre>
 */
@ConfigurationProperties(prefix = "lifepilot.ai")
public class AiProviderProperties {

    /** Provider identifier: {@code mock} or {@code openai}. */
    private String provider = "mock";

    private OpenAi openai = new OpenAi();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public OpenAi getOpenai() {
        return openai;
    }

    public void setOpenai(OpenAi openai) {
        this.openai = openai;
    }

    public static class OpenAi {

        private String apiKey = "";
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
        private double temperature = 0.2;
        private int maxTokens = 1024;
        private int timeoutSeconds = 30;
        private int retryMaxAttempts = 2;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public int getRetryMaxAttempts() {
            return retryMaxAttempts;
        }

        public void setRetryMaxAttempts(int retryMaxAttempts) {
            this.retryMaxAttempts = retryMaxAttempts;
        }
    }
}