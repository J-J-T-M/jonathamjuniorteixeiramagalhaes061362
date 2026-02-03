package com.jonathamjtm.gestaoartistas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gestão de Artistas API - Desafio Técnico SEPLAG",
                version = "v1.0",
                description = "API RESTful para controle de discografia (Teste Sênior). " +
                        "Implementa arquitetura em camadas, segurança JWT, " +
                        "armazenamento em MinIO e notificações real-time via WebSocket.",
                contact = @Contact(
                        name = "Jonatham Junior",
                        email = "jonatham.junior.18@gmail.com"
                )
        )
)

@SecurityScheme(
        name = "bearer-key",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Insira o token JWT gerado no login para autenticar as requisições nos endpoints protegidos."
)
public class OpenApiConfiguration { }