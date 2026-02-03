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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "4. Albums – Media", description = "Gerenciamento de mídias e capas de álbuns")
@SecurityRequirement(name = "bearer-key")
public class AlbumImageController {

    private final FileStorageService fileStorageService;
    private final AlbumService albumService;

    @GetMapping("/{id}/cover")
    @Operation(summary = "Obter Capa", description = "Retorna uma URL temporária (30min) para baixar a imagem")
    public ResponseEntity<Map<String, String>> getCoverUrl(@PathVariable Long id) {

        String fileName = albumService.findImageNameByAlbumId(id);

        String url = fileStorageService.getPresignedUrl(fileName);

        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de Capa", description = "Permite envio de UMA ou MAIS imagens (List<MultipartFile>).")
    public ResponseEntity<List<String>> uploadCover(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {

        List<String> savedFiles = albumService.uploadAlbumCovers(id, files);
        return ResponseEntity.ok(savedFiles);
    }
}