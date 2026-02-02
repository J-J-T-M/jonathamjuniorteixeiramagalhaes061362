package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.ExternalRegionalDTO;
import com.jonathamjtm.gestaoartistas.entity.Regional;
import com.jonathamjtm.gestaoartistas.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${integration.regionais.url}")
    private String apiUrl;

    /**
     * Executa a sincronização em background.
     * Requisito Sênior: "Atributo alterado -> inativar antigo e criar novo registro"
     */
    @Async("jobExecutor")
    @Transactional
    public void executeSyncJob() {
        log.info("JOB INICIADO: Sincronização de regionais na thread: {}", Thread.currentThread().getName());
        long start = System.currentTimeMillis();

        try {
            ExternalRegionalDTO[] response = restTemplate.getForObject(apiUrl, ExternalRegionalDTO[].class);

            if (response != null) {
                syncRegionals(Arrays.asList(response));
            }

            long time = System.currentTimeMillis() - start;
            log.info("JOB FINALIZADO: Sincronização concluída em {} ms", time);

        } catch (Exception e) {
            log.error("JOB FALHOU: Erro ao sincronizar regionais: {}", e.getMessage());
        }
    }

    private void syncRegionals(List<ExternalRegionalDTO> externos) {
        List<Regional> atuais = regionalRepository.findByActiveTrue();

        for (ExternalRegionalDTO ext : externos) {
            Optional<Regional> existenteOpt = atuais.stream()
                    .filter(r -> r.getRegionalId().equals(ext.getId()))
                    .findFirst();

            if (existenteOpt.isPresent()) {
                Regional existente = existenteOpt.get();

                if (!existente.getName().equals(ext.getNome())) {
                    log.info("Regional alterada (Versionamento): ID {} | {} -> {}", ext.getId(), existente.getName(), ext.getNome());

                    existente.setActive(false);
                    regionalRepository.save(existente);

                    Regional novo = Regional.builder()
                            .regionalId(ext.getId())
                            .name(ext.getNome())
                            .active(true)
                            .build();
                    regionalRepository.save(novo);
                }

                atuais.remove(existente);
            } else {
                log.info("Nova Regional detectada: ID {} - {}", ext.getId(), ext.getNome());
                Regional novo = Regional.builder()
                        .regionalId(ext.getId())
                        .name(ext.getNome())
                        .active(true)
                        .build();
                regionalRepository.save(novo);
            }
        }

        for (Regional sobrou : atuais) {
            log.info("Regional removida na origem (Inativando): ID {} - {}", sobrou.getRegionalId(), sobrou.getName());
            sobrou.setActive(false);
            regionalRepository.save(sobrou);
        }
    }
}