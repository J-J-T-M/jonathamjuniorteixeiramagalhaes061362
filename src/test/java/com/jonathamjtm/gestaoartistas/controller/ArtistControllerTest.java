package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArtistControllerTest extends BaseIntegrationTest {

    @Autowired private ArtistRepository artistRepository;

    @BeforeEach
    void setup() {
        artistRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /artists - Deve filtrar por NOME ignorando Maiúsculas/Minúsculas")
    void shouldFilterByNameCaseInsensitive() throws Exception {
        // ARRANGE
        artistRepository.save(Artist.builder().name("Guns N' Roses").build());
        artistRepository.save(Artist.builder().name("L.A. Guns").build());
        artistRepository.save(Artist.builder().name("Bon Jovi").build());

        // ACT e ASSERT
        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .param("name", "guns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Guns N' Roses"))
                .andExpect(jsonPath("$[1].name").value("L.A. Guns"));
    }

    @Test
    @DisplayName("GET /artists - Deve ORDENAR resultados (Z-A)")
    void shouldSortArtistsDesc() throws Exception {
        // ARRANGE
        artistRepository.save(Artist.builder().name("Alpha").build());
        artistRepository.save(Artist.builder().name("Beta").build());
        artistRepository.save(Artist.builder().name("Omega").build());

        // ACT
        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Omega"))
                .andExpect(jsonPath("$[1].name").value("Beta"))
                .andExpect(jsonPath("$[2].name").value("Alpha"));
    }

    @Test
    @DisplayName("GET /artists - Deve filtrar por DATA DE CRIAÇÃO (Recentes)")
    void shouldFilterByCreatedAfter() throws Exception {
        // ARRANGE
        Artist antigo = new Artist(null, "Velho Artista", null, null, null);
        antigo.setCreatedAt(LocalDateTime.now().minusDays(10));
        artistRepository.save(antigo);

        Artist novo = new Artist(null, "Novo Artista", null, null, null);
        novo.setCreatedAt(LocalDateTime.now());
        artistRepository.save(novo);

        String dataOntem = LocalDateTime.now().minusDays(1).toString();

        // ACT
        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .param("createdAfter", dataOntem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Só deve vir o novo
                .andExpect(jsonPath("$[0].name").value("Novo Artista"));
    }
}