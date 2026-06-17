package com.lifepilot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI lifePilotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifePilot API")
                        .description("AI 个人生活管家平台 API")
                        .version("0.1.0"));
    }
}