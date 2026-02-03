package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.dto.request.ArtistRequest;
import com.jonathamjtm.gestaoartistas.dto.response.ArtistResponse;
import com.jonathamjtm.gestaoartistas.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "2. Artistas", description = "Gestão de bandas e cantores")
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    @Operation(summary = "Listar artistas com filtros")
    public ResponseEntity<List<ArtistResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDateTime createdAfter,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(artistService.findAll(name, createdAfter, sortDirection));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar artista por ID")
    public ResponseEntity<ArtistResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo artista")
    public ResponseEntity<ArtistResponse> create(@RequestBody @Valid ArtistRequest request) {
        ArtistResponse response = artistService.createArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista")
    public ResponseEntity<ArtistResponse> update(@PathVariable Long id, @RequestBody @Valid ArtistRequest request) {
        return ResponseEntity.ok(artistService.updateArtist(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover artista")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}