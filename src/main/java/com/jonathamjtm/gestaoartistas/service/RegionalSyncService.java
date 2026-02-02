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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;

    private final RestTemplate restTemplate;

    @Value("${integration.regionais.url}")
    private String apiUrl;

    @Async("jobExecutor")
    @Transactional
    public void syncRegionals() {
        log.info("JOB INICIADO: Sincronização de regionais...");

        try {
            ExternalRegionalDTO[] response = restTemplate.getForObject(apiUrl, ExternalRegionalDTO[].class);

            if (response != null) {
                Arrays.stream(response).forEach(this::processSingleRegional);

                inactivateMissingRegionals(response);
            }
            log.info("JOB FINALIZADO: Sincronização concluída.");

        } catch (Exception e) {
            log.error("JOB FALHOU: Erro ao sincronizar regionais: {}", e.getMessage());
        }
    }

    private void processSingleRegional(ExternalRegionalDTO dto) {
        Optional<Regional> existingOpt = regionalRepository.findByExternalIdAndActiveTrue(dto.getId());

        if (existingOpt.isPresent()) {
            Regional existing = existingOpt.get();

            if (!existing.getName().equalsIgnoreCase(dto.getNome())) {
                log.info("ALTERAÇÃO DETECTADA: '{}' -> '{}'. Versionando...", existing.getName(), dto.getNome());

                existing.setActive(false);
                regionalRepository.save(existing);

                createNewRegional(dto);
            }
        } else {
            createNewRegional(dto);
        }
    }

    private void createNewRegional(ExternalRegionalDTO dto) {
        Regional newRegional = new Regional();
        newRegional.setExternalId(dto.getId());
        newRegional.setName(dto.getNome());
        newRegional.setActive(true);

        regionalRepository.save(newRegional);
    }

    private void inactivateMissingRegionals(ExternalRegionalDTO[] remoteList) {
        Set<Long> remoteIds = Arrays.stream(remoteList)
                .map(ExternalRegionalDTO::getId)
                .collect(Collectors.toSet());

        for (Regional local : regionalRepository.findByActiveTrue()) {
            if (!remoteIds.contains(local.getExternalId())) {
                local.setActive(false);
                regionalRepository.save(local);
            }
        }
    }
}