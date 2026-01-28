package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.AlbumRequest;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumControllerTest extends BaseIntegrationTest {

    @Autowired private ArtistRepository artistRepository;
    @MockBean private FileStorageService fileStorageService;

    @Test
    @DisplayName("POST /albums - Deve criar álbum vinculado a artista")
    void shouldCreateAlbumWithArtist() throws Exception {
        Artist artist = artistRepository.save(Artist.builder().name("Linkin Park").build());

        AlbumRequest request = new AlbumRequest();
        request.setTitle("Meteora");
        request.setReleaseYear(2003);
        request.setArtistIds(List.of(artist.getId()));

        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", generateAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Meteora"))
                .andExpect(jsonPath("$.artists[0].name").value("Linkin Park"));
    }
}