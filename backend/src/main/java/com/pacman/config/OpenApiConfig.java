package com.pacman.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置类
 * 访问地址: /swagger-ui.html 或 /swagger-ui/index.html
 * API 文档 JSON: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Token";
        
        return new OpenAPI()
                .info(new Info()
                        .title("吃豆人游戏系统 API")
                        .version("1.0.0")
                        .description("""
                                吃豆人游戏系统后端 API 文档
                                
                                ## 认证方式
                                除公开接口外，其他接口需要在请求头中携带 JWT Token：
                                ```
                                Authorization: Bearer <token>
                                ```
                                
                                ## 公开接口（无需认证）
                                - POST /api/auth/login - 用户登录
                                - POST /api/auth/register - 用户注册
                                - POST /api/auth/admin/login - 管理员登录
                                - GET /api/game/ranking - 排行榜
                                - GET /api/item/list - 道具列表
                                - POST /api/payment/alipay/notify - 支付宝回调
                                """)
                        .contact(new Contact()
                                .name("Pacman Team")
                                .email("support@pacman.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发服务器")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("输入 JWT Token（不需要 Bearer 前缀）")));
    }
}
