package com.jonathamjtm.gestaoartistas.controller;

import com.jonathamjtm.gestaoartistas.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais (Integração)", description = "Gerenciamento da sincronização com API Externa")
@SecurityRequirement(name = "bearer-key") // Protege com Token (Só admin deveria fazer isso)
public class RegionalController {

    private final RegionalSyncService regionalSyncService;

    @PostMapping("/sync")
    @Operation(summary = "Forçar Sincronização", description = "Dispara manualmente a rotina de importação das regionais da API externa.")
    public ResponseEntity<String> forceSync() {
        // Chama o mesmo método que o agendador chama
        regionalSyncService.syncRegionals();

        return ResponseEntity.ok("Sincronização realizada com sucesso!");
    }
}