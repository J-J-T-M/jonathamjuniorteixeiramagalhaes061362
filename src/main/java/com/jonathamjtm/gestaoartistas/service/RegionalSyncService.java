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

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${integration.regionais.url}")
    private String regionalsUrl;

    /**
     * @Async("jobExecutor"): Roda em background para não travar a API.
     */
    @Async("jobExecutor")
    @Transactional
    public void syncRegionals() {
        log.info("JOB INICIADO: Sincronização de regionais...");

        try {
            ExternalRegionalDTO[] regionals = restTemplate.getForObject(regionalsUrl, ExternalRegionalDTO[].class);

            if (regionals != null) {
                Arrays.stream(regionals).forEach(this::processRegional);
            }

            log.info("JOB FINALIZADO: Sincronização concluída com sucesso.");
        } catch (Exception e) {
            log.error("JOB FALHOU: Erro ao sincronizar regionais: {}", e.getMessage());
        }
    }

    private void processRegional(ExternalRegionalDTO dto) {
        Optional<Regional> existingOpt = regionalRepository.findByExternalIdAndActiveTrue(dto.getId());

        if (existingOpt.isPresent()) {
            Regional existing = existingOpt.get();

            if (!existing.getName().equalsIgnoreCase(dto.getNome())) {
                log.info("Regional {} alterada. Versionando...", dto.getId());

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
        log.info("Nova versão da Regional criada: ID Externo={} -> ID Banco={}", dto.getId(), newRegional.getId());
    }
}