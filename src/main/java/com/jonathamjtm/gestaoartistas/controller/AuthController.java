package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.dto.AuthResponse;
import com.jonathamjtm.gestaoartistas.dto.LoginRequest;
import com.jonathamjtm.gestaoartistas.dto.RegisterRequest;
import com.jonathamjtm.gestaoartistas.entity.User;
import com.jonathamjtm.gestaoartistas.repository.UserRepository;
import com.jonathamjtm.gestaoartistas.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor // Cria o construtor automaticamente (Injeção de Dependência)
@Tag(name = "Autenticação", description = "Endpoints para Login e Registro de usuários")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma conta de usuário e retorna o token JWT.")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: Este e-mail já está cadastrado.");
        }

        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);
        var token = tokenService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida credenciais e retorna o token de acesso JWT.")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        // Tenta autenticar (se falhar, o Spring lança erro 403 automaticamente)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var token = tokenService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }
}