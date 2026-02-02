package com.jonathamjtm.gestaoartistas.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor; // <--- Adicionado
import lombok.Data;
import lombok.NoArgsConstructor;  // <--- Adicionado

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "E-mail cadastrado", example = "admin@email.com")
    private String email;

    @NotBlank
    @Schema(description = "Senha do usuário", example = "123456")
    private String password;
}