package com.SymphonyAI.tasktracker.config;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openApi(AppProperties appProperties) {
        return new OpenAPI().info(new Info()
                .title(appProperties.getName())
                .version(appProperties.getVersion())
                .description("RESTful API for managing projects, users and tasks."));
    }
}
