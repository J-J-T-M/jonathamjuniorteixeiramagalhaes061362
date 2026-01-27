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
    @Operation(summary = "Listar Álbuns", description = "Lista todos os álbuns com seus respectivos artistas")
    public ResponseEntity<List<AlbumResponse>> listAll() {
        return ResponseEntity.ok(albumService.findAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Álbum", description = "Remove um álbum do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}