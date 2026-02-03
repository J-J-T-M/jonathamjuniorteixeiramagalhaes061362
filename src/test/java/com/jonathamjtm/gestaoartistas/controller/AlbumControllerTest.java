package com.jonathamjtm.gestaoartistas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.request.AlbumRequest;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.entity.ArtistType;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.service.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumControllerTest extends BaseIntegrationTest {

    @Autowired private AlbumRepository albumRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private MinioStorageService minioStorageService;

    @BeforeEach
    void setup() {
        albumRepository.deleteAll();
        artistRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve filtrar por TÍTULO (Parcial e Case Insensitive)")
    void shouldFilterByTitle() throws Exception {
        // ARRANGE
        Artist art = artistRepository.save(Artist.builder().name("Band").type(ArtistType.BAND).build());
        createAlbum("Meteora", 2003, art);
        createAlbum("Hybrid Theory", 2000, art);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("title", "met"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Meteora"));
    }

    @Test
    @DisplayName("Deve filtrar por ANO DE LANÇAMENTO (Maior ou Igual)")
    void shouldFilterByReleaseYear() throws Exception {
        // ARRANGE
        Artist art = artistRepository.save(Artist.builder().name("Band").type(ArtistType.BAND).build());
        createAlbum("Album 90", 1990, art);
        createAlbum("Album 2000", 2000, art);
        createAlbum("Album 2005", 2005, art);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("releaseYear", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve filtrar COMBINADO (Artista + Ano)")
    void shouldFilterByArtistAndYear() throws Exception {
        // ARRANGE
        Artist linkin = artistRepository.save(Artist.builder().name("Linkin Park").type(ArtistType.BAND).build());
        Artist beatles = artistRepository.save(Artist.builder().name("The Beatles").type(ArtistType.BAND).build());

        createAlbum("Meteora", 2003, linkin);
        createAlbum("Hybrid Theory", 2000, linkin);
        createAlbum("Abbey Road", 1969, beatles);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("artistId", linkin.getId().toString())
                        .param("releaseYear", "2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Meteora"));
    }

    @Test
    @DisplayName("Deve retornar LISTA VAZIA se nenhum filtro der match")
    void shouldReturnEmptyList() throws Exception {
        // ARRANGE
        Artist art = artistRepository.save(Artist.builder().name("Band").type(ArtistType.BAND).build());
        createAlbum("Meteora", 2003, art);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("title", "Xuxa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Deve ORDENAR por Título DESC")
    void shouldSortByTitleDesc() throws Exception {
        // ARRANGE
        Artist art = artistRepository.save(Artist.builder().name("Band").type(ArtistType.BAND).build());
        createAlbum("A - First", 2000, art);
        createAlbum("Z - Last", 2000, art);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Z - Last"))
                .andExpect(jsonPath("$.content[1].title").value("A - First"));
    }

    @Test
    @DisplayName("Deve PAGINAR corretamente")
    void shouldPaginateCorrectly() throws Exception {
        // ARRANGE
        Artist art = artistRepository.save(Artist.builder().name("Band").type(ArtistType.BAND).build());
        for (int i = 1; i <= 15; i++) {
            createAlbum("Vol " + i, 2000 + i, art);
        }

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("GET /albums/{id} - Deve retornar álbum por ID")
    void shouldGetAlbumById() throws Exception {
        // ARRANGE
        Album album = new Album();
        album.setTitle("Busca ID");
        album = albumRepository.save(album);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/albums/" + album.getId())
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Busca ID"));
    }

    @Test
    @DisplayName("DELETE /albums/{id} - Deve deletar álbum")
    void shouldDeleteAlbum() throws Exception {
        // ARRANGE
        Album album = new Album();
        album.setTitle("Para Deletar");
        album = albumRepository.save(album);

        // ACT
        mockMvc.perform(delete("/api/v1/albums/" + album.getId())
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isNoContent());

        // ASSERT
        mockMvc.perform(get("/api/v1/albums/" + album.getId())
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /albums - Deve criar um álbum novo")
    void shouldCreateAlbum() throws Exception {
        // ARRANGE
        Artist art = artistRepository.save(Artist.builder().name("Artist Create").type(ArtistType.SINGER).build());

        AlbumRequest request = new AlbumRequest();
        request.setTitle("Novo Album");
        request.setReleaseYear(2024);
        request.setArtistIds(List.of(art.getId()));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Novo Album")));
    }

    @Test
    @DisplayName("PUT /albums/{id} - Deve ATUALIZAR um álbum existente")
    void shouldUpdateAlbum() throws Exception {
        // ARRANGE
        Artist artist = artistRepository.save(Artist.builder().name("Artist Old").type(ArtistType.BAND).build());
        Album album = new Album();
        album.setTitle("Titulo Antigo");
        album.setReleaseYear(2000);
        album.setArtists(Set.of(artist));
        album = albumRepository.save(album);

        AlbumRequest updateRequest = new AlbumRequest();
        updateRequest.setTitle("Titulo Atualizado");
        updateRequest.setReleaseYear(2025);
        updateRequest.setArtistIds(List.of(artist.getId()));

        // ACT & ASSERT
        mockMvc.perform(put("/api/v1/albums/" + album.getId())
                        .header("Authorization", gerarTokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Titulo Atualizado")))
                .andExpect(jsonPath("$.releaseYear", is(2025)));
    }

    @Test
    @DisplayName("POST /albums - Deve retornar ERRO 400 ao criar álbum inválido")
    void shouldReturnBadRequestForInvalidAlbum() throws Exception {
        // ARRANGE
        AlbumRequest invalidRequest = new AlbumRequest();
        invalidRequest.setReleaseYear(2024);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private void createAlbum(String title, int year, Artist artist) {
        Album album = new Album();
        album.setTitle(title);
        album.setReleaseYear(year);
        album.setArtists(Set.of(artist));
        albumRepository.save(album);
    }
}