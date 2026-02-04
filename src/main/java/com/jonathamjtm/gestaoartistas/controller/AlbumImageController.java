package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.service.AlbumService;
import com.jonathamjtm.gestaoartistas.service.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "4. Albums – Media", description = "Gerenciamento de mídias e capas de álbuns")
@SecurityRequirement(name = "bearer-key")
public class AlbumImageController {

    private final FileStorageService fileStorageService;
    private final AlbumService albumService;

    @GetMapping("/{id}/cover")
    @Operation(summary = "Obter Capas", description = "Retorna lista de URLs assinadas das capas")
    public ResponseEntity<List<String>> getCoverUrls(@PathVariable Long id) {

        List<String> fileNames = albumService.findAllImageNamesByAlbumId(id);

        List<String> urls = fileNames.stream()
                .map(fileStorageService::getPresignedUrl)
                .toList();

        return ResponseEntity.ok(urls);
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de Capas", description = "Permite envio de uma ou mais imagens.")
    public ResponseEntity<List<String>> uploadCover(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {

        List<String> savedFileNames = albumService.uploadAlbumCovers(id, files);

        List<String> fileUrls = savedFileNames.stream()
                .map(fileStorageService::getPresignedUrl)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(fileUrls);
    }
}