package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "Álbuns - Imagens", description = "Upload de capas")
@SecurityRequirement(name = "bearer-key")
public class AlbumImageController {

    private final AlbumService albumService;

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de Capa", description = "Envia uma imagem (jpg/png) para o álbum. Salva no MinIO.")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {

        albumService.uploadAlbumCover(id, file);
        return ResponseEntity.ok("Upload realizado com sucesso!");
    }
}