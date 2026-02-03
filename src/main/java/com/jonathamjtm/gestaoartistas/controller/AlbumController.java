package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.dto.request.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.response.AlbumResponse;
import com.jonathamjtm.gestaoartistas.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "3. Álbuns", description = "Gerenciamento de discos e lançamentos")
@SecurityRequirement(name = "bearer-key")
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    @Operation(summary = "Listar Álbuns (Busca Avançada)", description = "Filtre por título, artista e ano de lançamento.")
    public ResponseEntity<Page<AlbumResponse>> listAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Integer releaseYear,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        return ResponseEntity.ok(albumService.searchAlbums(title, releaseYear, artistId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Álbum por ID")
    public ResponseEntity<AlbumResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.getAlbumById(id));
    }

    @PostMapping
    @Operation(summary = "Criar Álbum", description = "Cadastra álbum e vincula a um ou mais artistas")
    public ResponseEntity<AlbumResponse> create(@RequestBody @Valid AlbumRequest request) {
        var response = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Álbum")
    public ResponseEntity<AlbumResponse> update(@PathVariable Long id, @RequestBody @Valid AlbumRequest request) {
        return ResponseEntity.ok(albumService.updateAlbum(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Álbum")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}