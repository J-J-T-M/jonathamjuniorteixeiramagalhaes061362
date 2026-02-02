package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumControllerTest extends BaseIntegrationTest {

    @Autowired private AlbumRepository albumRepository;
    @Autowired private ArtistRepository artistRepository;

    @MockitoBean private FileStorageService fileStorageService;

    @BeforeEach
    void setup() {
        albumRepository.deleteAll();
        artistRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve filtrar por TÍTULO (Parcial e Case Insensitive)")
    void shouldFilterByTitle() throws Exception {
        Artist art = artistRepository.save(new Artist(null, "Band", null, null, null));
        createAlbum("Meteora", 2003, art);
        createAlbum("Hybrid Theory", 2000, art);

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
        Artist art = artistRepository.save(new Artist(null, "Band", null, null, null));
        createAlbum("Album 90", 1990, art);
        createAlbum("Album 2000", 2000, art);
        createAlbum("Album 2005", 2005, art);

        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("releaseYear", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve filtrar COMBINADO (Artista + Ano)")
    void shouldFilterByArtistAndYear() throws Exception {
        Artist linkin = artistRepository.save(Artist.builder().name("Linkin Park").build());
        Artist beatles = artistRepository.save(Artist.builder().name("The Beatles").build());

        createAlbum("Meteora", 2003, linkin);
        createAlbum("Hybrid Theory", 2000, linkin);
        createAlbum("Abbey Road", 1969, beatles);

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
        Artist art = artistRepository.save(new Artist(null, "Band", null, null, null));
        createAlbum("Meteora", 2003, art);

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
        Artist art = artistRepository.save(new Artist(null, "Band", null, null, null));
        createAlbum("A - First", 2000, art);
        createAlbum("Z - Last", 2000, art);

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
        Artist art = artistRepository.save(new Artist(null, "Band", null, null, null));
        for (int i = 1; i <= 15; i++) {
            createAlbum("Vol " + i, 2000 + i, art);
        }

        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", gerarTokenAdmin())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    private void createAlbum(String title, int year, Artist artist) {
        Album album = new Album();
        album.setTitle(title);
        album.setReleaseYear(year);
        album.setArtists(Set.of(artist));
        albumRepository.save(album);
    }
}