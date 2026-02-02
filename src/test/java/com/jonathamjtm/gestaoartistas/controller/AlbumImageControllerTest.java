package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.AlbumImage;
import com.jonathamjtm.gestaoartistas.repository.AlbumImageRepository;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.service.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumImageControllerTest extends BaseIntegrationTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumImageRepository albumImageRepository;

    @MockitoBean
    private MinioStorageService minioStorageService;

    @BeforeEach
    void setup() {
        albumImageRepository.deleteAll();
        albumRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/albums/{id}/cover - Deve fazer upload da capa com sucesso")
    void shouldUploadAlbumCover() throws Exception {
        Album album = new Album();
        album.setTitle("Album Teste Upload");
        album = albumRepository.save(album);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo-da-imagem".getBytes()
        );

        given(minioStorageService.upload(any())).willReturn("nome-arquivo-uuid.jpg");

        mockMvc.perform(multipart("/api/v1/albums/" + album.getId() + "/cover")
                        .file(file)
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/albums/{id}/cover - Deve retornar URL assinada")
    void shouldGetCoverUrl() throws Exception {
        Album album = new Album();
        album.setTitle("Album URL");
        album = albumRepository.save(album);

        AlbumImage image = AlbumImage.builder()
                .album(album)
                .fileName("arquivo-no-minio.jpg")
                .contentType("image/jpeg")
                .build();
        albumImageRepository.save(image);

        String urlFalsa = "http://localhost:9000/bucket/arquivo-no-minio.jpg?token=123";
        given(minioStorageService.getPresignedUrl(image.getFileName())).willReturn(urlFalsa);

        mockMvc.perform(get("/api/v1/albums/" + album.getId() + "/cover")
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(urlFalsa));
    }
}