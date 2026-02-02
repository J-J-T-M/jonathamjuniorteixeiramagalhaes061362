package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.service.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumImageControllerTest extends BaseIntegrationTest {

    @Autowired private AlbumRepository albumRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("POST /albums/{id}/cover - Deve fazer upload da capa com sucesso")
    void shouldUploadAlbumCover() throws Exception {
        // ARRANGE
        Album album = new Album();
        album.setTitle("Album Teste");
        albumRepository.save(album);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo-da-imagem-falsa".getBytes()
        );

        given(fileStorageService.upload(any())).willReturn("uuid-capa.jpg");

        // ACT & ASSERT
        mockMvc.perform(multipart("/api/v1/albums/" + album.getId() + "/cover")
                        .file(file)
                        .header("Authorization", gerarTokenAdmin()))
                .andExpect(status().isOk());
    }
}