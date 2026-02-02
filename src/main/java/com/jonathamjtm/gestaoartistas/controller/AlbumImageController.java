package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.service.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/albums") //
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Gerenciamento de capas")
@SecurityRequirement(name = "bearer-key")
public class AlbumImageController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de Capa", description = "Envia a imagem da capa do álbum")
    public ResponseEntity<String> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.upload(file);
        return ResponseEntity.ok("Imagem salva: " + fileName);
    }
}