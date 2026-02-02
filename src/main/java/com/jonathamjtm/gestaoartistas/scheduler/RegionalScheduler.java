package com.jonathamjtm.gestaoartistas.scheduler;

import com.jonathamjtm.gestaoartistas.service.RegionalSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class RegionalScheduler {

    private final RegionalSyncService regionalSyncService;

    // Roda de tempos em tempos (configurado no application.properties)
    @Scheduled(fixedRateString = "${integration.regionais.fixed-rate}")
    public void scheduleSync() {
        log.info("Agendador disparou o job de sincronização...");
        // CORREÇÃO: Chamando o método novo 'syncRegionals'
        regionalSyncService.syncRegionals();
    }

    // Roda uma vez assim que a aplicação sobe
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Aplicação iniciada! Disparando job inicial em background...");
        regionalSyncService.syncRegionals();
    }
}