package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.dto.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.AlbumResponse;
import com.jonathamjtm.gestaoartistas.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Gerenciamento de discos e lançamentos")
@SecurityRequirement(name = "bearer-key")
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    @Operation(summary = "Criar Álbum", description = "Cadastra álbum e vincula a um ou mais artistas")
    public ResponseEntity<AlbumResponse> create(@RequestBody @Valid AlbumRequest request) {
        var response = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar Álbuns (Busca Avançada)", description = "Filtre por título, artista, ano de lançamento (>=) e data de criação (>=).")
    public ResponseEntity<Page<AlbumResponse>> listAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Integer releaseYear, // Busca deste ano para frente
            @RequestParam(required = false) LocalDateTime createdAfter,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        return ResponseEntity.ok(albumService.findAll(title, artistId, releaseYear, createdAfter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Álbum por ID", description = "Retorna os detalhes de um álbum específico")
    public ResponseEntity<AlbumResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Álbum", description = "Atualiza título, ano e lista de artistas")
    public ResponseEntity<AlbumResponse> update(@PathVariable Long id, @RequestBody @Valid AlbumRequest request) {
        return ResponseEntity.ok(albumService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Álbum", description = "Remove um álbum do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}