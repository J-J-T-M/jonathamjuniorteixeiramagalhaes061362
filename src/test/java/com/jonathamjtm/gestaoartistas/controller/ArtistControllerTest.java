package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.ArtistRequest;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArtistControllerTest extends BaseIntegrationTest {

    @Autowired private ArtistRepository artistRepository;

    @BeforeEach
    void setup() {
        artistRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /artists - Deve criar artista com sucesso (Token Válido)")
    void shouldCreateArtist() throws Exception {
        ArtistRequest request = new ArtistRequest();
        request.setName("System of a Down");

        mockMvc.perform(post("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("System of a Down"));
    }

    @Test
    @DisplayName("GET /artists - Deve filtrar por nome e ordenar DESC")
    void shouldFilterAndSortArtists() throws Exception {
        artistRepository.save(new Artist(null, "Alice in Chains", null, null, null));
        artistRepository.save(new Artist(null, "Audioslave", null, null, null));
        artistRepository.save(new Artist(null, "Metallica", null, null, null));

        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", gerarTokenAdmin())
                        .param("name", "audio")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Audioslave"));
    }

    @Test
    @DisplayName("POST /artists - Deve retornar 403 Forbidden sem token")
    void shouldBlockUnauthenticatedRequest() throws Exception {
        ArtistRequest request = new ArtistRequest();
        request.setName("Hacker Band");

        mockMvc.perform(post("/api/v1/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}