package com.jonathamjtm.gestaoartistas.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "E-mail cadastrado", example = "admin@email.com")
    private String email;

    @NotBlank
    @Schema(description = "Senha do usuário", example = "123456")
    private String password;
}