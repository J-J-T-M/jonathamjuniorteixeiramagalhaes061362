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
                title = "Gestão de Artistas API",
                version = "v1",
                description = "API para controle de discografia (Teste Sênior)",
                contact = @Contact(
                        name = "Seu Nome",
                        email = "seu.email@exemplo.com"
                )
        )
)

@SecurityScheme(
        name = "bearer-key",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Insira o token JWT aqui para autenticar as requisições."
)
public class OpenApiConfiguration {
}