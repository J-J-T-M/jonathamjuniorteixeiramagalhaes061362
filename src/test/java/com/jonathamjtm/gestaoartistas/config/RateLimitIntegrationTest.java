package com.jonathamjtm.gestaoartistas.config;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.auth.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// Importe o LoginRequest para enviar um corpo válido (opcional, mas boa prática)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // <--- USE POST
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "security.rate-limit.capacity=10",
        "security.rate-limit.refill-tokens=10",
        "security.rate-limit.refill-period=1"
})
class RateLimitIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve permitir até 10 requisições e bloquear a 11ª (Rate Limit)")
    void shouldBlockAfterLimitExceeded() throws Exception {

        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(400));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests());
    }
}