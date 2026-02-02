package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.entity.Regional;
import com.jonathamjtm.gestaoartistas.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegionalControllerTest extends BaseIntegrationTest {

    @Autowired
    private RegionalRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        repository.save(Regional.builder()
                .regionalId(101L)
                .name("Cuiabá")
                .active(true)
                .build());

        repository.save(Regional.builder()
                .regionalId(102L)
                .name("Rondonópolis (Antiga)")
                .active(false)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/regionais - Deve trazer TODAS se não filtrar")
    void shouldListAll() throws Exception {
        mockMvc.perform(get("/api/v1/regionais")
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/regionais?active=true - Deve trazer APENAS ATIVAS")
    void shouldListOnlyActive() throws Exception {
        mockMvc.perform(get("/api/v1/regionais")
                        .param("active", "true")
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Cuiabá"));
    }
}