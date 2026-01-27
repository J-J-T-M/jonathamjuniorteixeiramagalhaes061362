package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.dto.ArtistRequest;
import com.jonathamjtm.gestaoartistas.dto.ArtistResponse;
import com.jonathamjtm.gestaoartistas.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "Gerenciamento de bandas e artistas")
@SecurityRequirement(name = "bearer-key")
public class ArtistController {

    private final ArtistService artistService;

    @PostMapping
    @Operation(summary = "Criar Artista", description = "Cadastra um novo artista e notifica via WebSocket")
    public ResponseEntity<ArtistResponse> create(@RequestBody @Valid ArtistRequest request) {
        var response = artistService.createArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar Artistas", description = "Retorna todos os artistas cadastrados")
    public ResponseEntity<List<ArtistResponse>> listAll() {
        return ResponseEntity.ok(artistService.findAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Artista", description = "Remove um artista pelo ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}