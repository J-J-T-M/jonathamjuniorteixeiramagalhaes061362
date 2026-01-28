package com.jonathamjtm.gestaoartistas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathamjtm.gestaoartistas.service.TokenService;
import com.jonathamjtm.gestaoartistas.entity.User;
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

    protected String gerarTokenAdmin() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setId(1L);
        return "Bearer " + tokenService.generateToken(user);
    }
}