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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${integration.regionais.url}")
    private String apiUrl;

    /**
     * @Async("jobExecutor"): Diz ao Spring: "Não rode isso na thread principal".
     * Jogue isso na fila 'jobExecutor' e libere quem chamou imediatamente.
     */
    @Async("jobExecutor")
    @Transactional
    public void executeSyncJob() {
        log.info("JOB INICIADO: Sincronização de regionais na thread: {}", Thread.currentThread().getName());
        long start = System.currentTimeMillis();

        try {
            ExternalRegionalDTO[] response = restTemplate.getForObject(apiUrl, ExternalRegionalDTO[].class);

            if (response != null) {
                processRegionals(response);
            }

            long time = System.currentTimeMillis() - start;
            log.info("JOB FINALIZADO: Sincronização concluída em {} ms", time);

        } catch (Exception e) {
            log.error("JOB FALHOU: Erro ao sincronizar regionais: {}", e.getMessage());
        }
    }

    // Separei a lógica de processamento para ficar mais limpo
    private void processRegionals(ExternalRegionalDTO[] response) {
        Map<Long, ExternalRegionalDTO> remoteMap = Arrays.stream(response)
                .collect(Collectors.toMap(ExternalRegionalDTO::getId, Function.identity()));

        List<Regional> localRegionals = regionalRepository.findByActiveTrue();
        Map<Long, Regional> localMap = localRegionals.stream()
                .collect(Collectors.toMap(Regional::getId, Function.identity()));

        // Processa Novos e Alterados
        for (ExternalRegionalDTO remote : remoteMap.values()) {
            Regional local = localMap.get(remote.getId());
            if (local == null) {
                createNewRegional(remote);
            } else if (!local.getName().equals(remote.getNome())) {
                log.info("Atualizando Regional ID {}: {} -> {}", remote.getId(), local.getName(), remote.getNome());
                local.setName(remote.getNome());
                regionalRepository.save(local);
            }
        }

        // Processa Inativos
        for (Regional local : localRegionals) {
            if (!remoteMap.containsKey(local.getId())) {
                log.info("Inativando Regional ID {}", local.getId());
                local.setActive(false);
                regionalRepository.save(local);
            }
        }
    }

    private void createNewRegional(ExternalRegionalDTO dto) {
        log.info("Criando Regional ID {}", dto.getId());
        regionalRepository.save(Regional.builder()
                .id(dto.getId())
                .name(dto.getNome())
                .active(true)
                .build());
    }
}