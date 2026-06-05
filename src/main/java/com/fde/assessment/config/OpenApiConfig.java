package com.fde.assessment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assessmentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 辅助研发工作项流转与需求澄清系统 API")
                        .description("FDE 后端考核 — 工作项管理、状态流转、澄清问题和 AI 辅助分析")
                        .version("1.0.0")
                        .contact(new Contact().name("FDE Candidate"))
                        .license(new License().name("Internal Use Only")));
    }
}
