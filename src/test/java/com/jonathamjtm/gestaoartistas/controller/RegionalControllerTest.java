package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.entity.Regional;
import com.jonathamjtm.gestaoartistas.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegionalControllerTest extends BaseIntegrationTest {

    @Autowired private RegionalRepository regionalRepository;

    @BeforeEach
    void setup() {
        regionalRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/regionais - Deve listar apenas regionais ATIVAS por padrão ou filtro")
    void shouldListRegionals() throws Exception {
        // ARRANGE
        regionalRepository.save(new Regional(null, 101L, "Cuiabá", true));
        regionalRepository.save(new Regional(null, 102L, "Várzea Grande", true));
        regionalRepository.save(new Regional(null, 101L, "Cuiabá (Antigo)", false)); // Histórico

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/regionais")
                        .param("active", "true")
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2))) // Só as 2 ativas
                .andExpect(jsonPath("$.content[0].active", is(true)));
    }

    @Test
    @DisplayName("GET /api/v1/regionais - Deve permitir listar histórico (Inativas)")
    void shouldListInactiveRegionals() throws Exception {
        // ARRANGE
        regionalRepository.save(new Regional(null, 101L, "Cuiabá", true));
        regionalRepository.save(new Regional(null, 101L, "Cuiabá (Antigo)", false));

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/regionais")
                        .param("active", "false")
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Cuiabá (Antigo)")));
    }
}