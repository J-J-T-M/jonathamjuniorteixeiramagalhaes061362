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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "2. Artists – Management", description = "Operações de cadastro e manutenção de bandas e artistas solo")
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
    @Operation(summary = "Listar Artistas", description = "Filtro por nome, data de criação e ordenação.")
    public ResponseEntity<List<ArtistResponse>> listAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDateTime createdAfter, // Formato ISO
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {

        return ResponseEntity.ok(artistService.findAll(name, createdAfter, sortDirection));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Artista", description = "Atualiza os dados de um artista existente")
    public ResponseEntity<ArtistResponse> update(@PathVariable Long id, @RequestBody @Valid ArtistRequest request) {
        return ResponseEntity.ok(artistService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Artista", description = "Remove um artista pelo ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}