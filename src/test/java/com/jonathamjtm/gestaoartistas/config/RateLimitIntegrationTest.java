package com.jonathamjtm.gestaoartistas.config;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("Deve permitir até 10 requisições POR USUÁRIO e bloquear a 11ª (HTTP 429)")
    void shouldBlockAfterLimitExceededForAuthenticatedUser() throws Exception {
        String token = gerarTokenAdmin();

        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(get("/api/v1/regionais")
                            .header("Authorization", token))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/regionais")
                        .header("Authorization", token))
                .andExpect(status().isTooManyRequests());
    }
}