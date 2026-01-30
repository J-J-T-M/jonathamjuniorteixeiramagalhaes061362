package com.jonathamjtm.gestaoartistas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathamjtm.gestaoartistas.entity.User;
import com.jonathamjtm.gestaoartistas.repository.UserRepository;
import com.jonathamjtm.gestaoartistas.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected TokenService tokenService;

    @Autowired protected UserRepository userRepository;

    protected String gerarTokenAdmin() {
        String email = "admin@test.com";


        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName("Admin Teste");
            newUser.setPassword("123456");
            newUser.setRole("ADMIN");
            return userRepository.save(newUser);
        });

        return "Bearer " + tokenService.generateToken(user);
    }
}