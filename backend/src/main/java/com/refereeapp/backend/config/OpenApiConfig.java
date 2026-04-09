package com.refereeapp.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RefMate API",
                version = "1.0",
                description = "Документація REST API для системи призначення баскетбольних арбітрів",
                contact = @Contact(name = "Євгеній Ольховський", email = "jekaolhovskii@gmail.com")
        ),
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Вставте сюди ваш JWT токен, отриманий при логіні (без слова Bearer)"
)
public class OpenApiConfig {
}