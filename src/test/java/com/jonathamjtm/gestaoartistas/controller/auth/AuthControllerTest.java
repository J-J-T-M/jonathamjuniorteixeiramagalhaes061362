package com.jonathamjtm.gestaoartistas.controller.auth;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.auth.LoginRequest;
import com.jonathamjtm.gestaoartistas.dto.auth.RefreshTokenRequest;
import com.jonathamjtm.gestaoartistas.dto.auth.RegisterRequest;
import com.jonathamjtm.gestaoartistas.entity.User;
import com.jonathamjtm.gestaoartistas.repository.UserRepository;
import com.jonathamjtm.gestaoartistas.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register - Deve criar usuário e retornar tokens (Sucesso)")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("Novo Usuario", "novo@email.com", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));

        User savedUser = userRepository.findByEmail("novo@email.com").orElseThrow();
        assertThat(savedUser.getFullName()).isEqualTo("Novo Usuario");
        assertThat(passwordEncoder.matches("123456", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 409 Conflict se email já existe")
    void shouldFailRegisterDuplicateEmail() throws Exception {
        User existing = new User();
        existing.setFullName("Existente");
        existing.setEmail("duplicado@email.com");
        existing.setPassword("123");
        existing.setRole("USER");
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest("Outro", "duplicado@email.com", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/login - Deve autenticar e retornar tokens (Sucesso)")
    void shouldLoginSuccessfully() throws Exception {
        User user = new User();
        user.setFullName("Login User");
        user.setEmail("login@test.com");
        user.setPassword(passwordEncoder.encode("senha123"));
        user.setRole("USER");
        userRepository.save(user);

        LoginRequest login = new LoginRequest("login@test.com", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/login - Deve falhar com credenciais inválidas")
    void shouldFailLoginBadCredentials() throws Exception {
        User user = new User();
        user.setFullName("Login User");
        user.setEmail("login@test.com");
        user.setPassword(passwordEncoder.encode("senha123"));
        user.setRole("USER");
        userRepository.save(user);

        LoginRequest login = new LoginRequest("login@test.com", "senhaERRADA");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /auth/refresh - Deve gerar NOVO Access Token com Refresh Token válido")
    void shouldRefreshAccessToken() throws Exception {
        User user = new User();
        user.setFullName("Refresh User");
        user.setEmail("refresh@test.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("USER");
        userRepository.save(user);

        String validRefreshToken = tokenService.generateRefreshToken(user);

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(validRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(validRefreshToken));
    }

    @Test
    @DisplayName("POST /auth/refresh - Deve rejeitar Refresh Token inválido/inexistente")
    void shouldRejectInvalidRefreshToken() throws Exception {
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("token.invalido.123");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isForbidden());
    }
}