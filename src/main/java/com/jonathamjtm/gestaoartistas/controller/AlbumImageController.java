package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.service.AlbumService;
import com.jonathamjtm.gestaoartistas.service.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "4. Albums – Media", description = "Gerenciamento de mídias e capas de álbuns")
@SecurityRequirement(name = "bearer-key")
public class AlbumImageController {

    private final FileStorageService fileStorageService;
    private final AlbumService albumService;

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de Capa", description = "Envia a imagem da capa do álbum")
    public ResponseEntity<String> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {

        String fileName = fileStorageService.upload(file);
        return ResponseEntity.ok("Imagem salva: " + fileName);
    }

    @GetMapping("/{id}/cover")
    @Operation(summary = "Obter Capa", description = "Retorna uma URL temporária (30min) para baixar a imagem")
    public ResponseEntity<Map<String, String>> getCoverUrl(@PathVariable Long id) {

        String fileName = albumService.findImageNameByAlbumId(id);

        String url = fileStorageService.getPresignedUrl(fileName);

        return ResponseEntity.ok(Map.of("url", url));
    }
}