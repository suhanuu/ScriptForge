package com.scriptforge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jDocxConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScriptForge API 文档")
                        .version("v2.0")
                        .description("AI 小说转剧本工具 - 接口文档")
                        .contact(new Contact()
                                .name("ScriptForge")
                                .url("https://github.com/dabidai/ScriptForge")));
    }
    @Bean
    public GroupedOpenApi novelsApi() {
        return GroupedOpenApi.builder()
                .group("小说管理")
                .pathsToMatch("/api/novels/**")
                .build();
    }

    @Bean
    public GroupedOpenApi scriptsApi() {
        return GroupedOpenApi.builder()
                .group("剧本转换")
                .pathsToMatch("/api/scripts/**")
                .build();
    }
    @Bean
    public GroupedOpenApi healthApi() {
        return GroupedOpenApi.builder()
                .group("健康检查")
                .pathsToMatch("/api/health")
                .build();
    }


}
