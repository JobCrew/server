package com.org.example.jobcrew.global.config.swagger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.web.bind.annotation.BindParam;
@Configuration
public class SwaggerConfig {
    // ✅ 소셜 로그인 API 그룹
    @Bean
    public GroupedOpenApi oauthApi() {
        return GroupedOpenApi.builder()
                .group("🌐 소셜 로그인 API")
                .pathsToMatch("/oauth2/docs/**")
                .addOpenApiCustomizer(jwtSecurityCustomizer())
                .build();
    }

    // ✅ 인증 API 그룹
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("🔐 인증 API")
                .pathsToMatch("/api/auth/**")
                .addOpenApiCustomizer(jwtSecurityCustomizer())
                .build();
    }

    // ✅ 마이페이지 API 그룹
    @Bean
    public GroupedOpenApi myPageApi() {
        return GroupedOpenApi.builder()
                .group("👤 마이페이지 API")
                .pathsToMatch("/api/v1/my/**")
                .addOpenApiCustomizer(jwtSecurityCustomizer())
                .build();
    }

    // ✅ API 메타정보
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JobCrew API")
                        .version("v1.0")
                        .description("JobCrew API 문서입니다."));
    }


    // ✅ JWT 보안 설정 커스터마이저
    private OpenApiCustomizer jwtSecurityCustomizer() {
        return openApi -> openApi.addSecurityItem(new SecurityRequirement().addList("jwt token"))
                .getComponents()
                .addSecuritySchemes("jwt token", new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .in(SecurityScheme.In.HEADER)
                        .bearerFormat("JWT")
                        .scheme("bearer"));
    }
}
