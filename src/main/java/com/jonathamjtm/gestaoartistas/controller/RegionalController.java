package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.entity.Regional;
import com.jonathamjtm.gestaoartistas.repository.RegionalRepository;
import com.jonathamjtm.gestaoartistas.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "5. Regionals – Synchronization", description = "Integração com serviços externos")
@SecurityRequirement(name = "bearer-key")
public class RegionalController {

    private final RegionalSyncService regionalSyncService;
    private final RegionalRepository regionalRepository;

    @GetMapping
    @Operation(summary = "Listar Regionais", description = "Lista as regionais sincronizadas. Permite filtrar por status (ativas/inativas).")
    public ResponseEntity<Page<Regional>> findAll(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Page<Regional> page;

        if (active != null) {
            page = regionalRepository.findByActive(active, pageable);
        } else {
            page = regionalRepository.findAll(pageable);
        }

        return ResponseEntity.ok(page);
    }

    @PostMapping("/sync")
    @Operation(summary = "Forçar Sincronização", description = "Dispara job em background (Async) para puxar dados da API externa.")
    public ResponseEntity<String> forceSync() {
        regionalSyncService.syncRegionals();
        return ResponseEntity.ok("Job de sincronização disparado com sucesso! Verifique os logs.");
    }
}