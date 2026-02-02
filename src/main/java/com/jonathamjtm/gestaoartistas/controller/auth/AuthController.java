package com.jonathamjtm.gestaoartistas.controller.auth;

import com.jonathamjtm.gestaoartistas.dto.auth.AuthResponse;
import com.jonathamjtm.gestaoartistas.dto.auth.LoginRequest;
import com.jonathamjtm.gestaoartistas.dto.auth.RefreshTokenRequest;
import com.jonathamjtm.gestaoartistas.dto.auth.RegisterRequest;
import com.jonathamjtm.gestaoartistas.entity.User;
import com.jonathamjtm.gestaoartistas.repository.UserRepository;
import com.jonathamjtm.gestaoartistas.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de Login, Registro e Refresh Token")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Operation(summary = "Registrar Usuário", description = "Cria um novo usuário no sistema.")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Default role

        userRepository.save(user);

        String jwtToken = tokenService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica usuário e retorna tokens de acesso.")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String jwtToken = tokenService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar Token", description = "Gera um novo Access Token a partir de um Refresh Token válido.")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        String userEmail = tokenService.extractUsername(requestRefreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário do token não encontrado"));

            if (tokenService.isTokenValid(requestRefreshToken, user)) {

                String newAccessToken = tokenService.generateToken(user);

                return ResponseEntity.ok(AuthResponse.builder()
                        .token(newAccessToken)
                        .refreshToken(requestRefreshToken)
                        .build());
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}