package com.jonathamjtm.gestaoartistas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.ArtistRequest;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArtistControllerTest extends BaseIntegrationTest {

    @Autowired private ArtistRepository artistRepository;
    @Autowired private ObjectMapper objectMapper;

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

        // ACT & ASSERT
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

        // ACT: Busca sem filtro, mas com ordenação DESC
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
        // ARRANGE: Cria um artista (a data será "agora" automaticamente pelo @CreationTimestamp)
        artistRepository.save(Artist.builder().name("Artista Recente").build());

        String dataOntem = LocalDateTime.now().minusDays(1).toString();
        String dataAmanha = LocalDateTime.now().plusDays(1).toString();

        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .param("createdAfter", dataOntem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Artista Recente"));

        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .param("createdAfter", dataAmanha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("PUT /artists - Deve ATUALIZAR nome do artista")
    void shouldUpdateArtist() throws Exception {
        // ARRANGE
        Artist artist = artistRepository.save(Artist.builder().name("Nome Antigo").build());
        ArtistRequest updateRequest = new ArtistRequest();
        updateRequest.setName("Nome Novo");

        // ACT & ASSERT
        mockMvc.perform(put("/api/v1/artists/" + artist.getId())
                        .header("Authorization", gerarTokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Novo"));
    }
}