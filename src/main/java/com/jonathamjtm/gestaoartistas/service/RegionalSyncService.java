package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.ExternalRegionalDTO;
import com.jonathamjtm.gestaoartistas.entity.Regional;
import com.jonathamjtm.gestaoartistas.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(fixedRateString = "${integration.regionais.fixed-rate}")
    @Transactional
    public void syncRegionals() {
        log.info("Iniciando sincronização de regionais na URL: {}", apiUrl);

        try {
            ExternalRegionalDTO[] response = restTemplate.getForObject(apiUrl, ExternalRegionalDTO[].class);

            if (response == null) return;


            Map<Long, ExternalRegionalDTO> remoteMap = Arrays.stream(response)
                    .collect(Collectors.toMap(ExternalRegionalDTO::getId, Function.identity()));

            List<Regional> localRegionals = regionalRepository.findByActiveTrue();
            Map<Long, Regional> localMap = localRegionals.stream()
                    .collect(Collectors.toMap(Regional::getId, Function.identity()));

            for (ExternalRegionalDTO remote : remoteMap.values()) {
                Regional local = localMap.get(remote.getId());

                if (local == null) {
                    createNewRegional(remote);
                } else if (!local.getName().equals(remote.getNome())) {
                    log.info("Regional alterada: ID {} ({} -> {})", remote.getId(), local.getName(), remote.getNome());
                    local.setName(remote.getNome());
                    regionalRepository.save(local);
                }
            }

            for (Regional local : localRegionals) {
                if (!remoteMap.containsKey(local.getId())) {
                    log.info("Regional inativada: ID {}", local.getId());
                    local.setActive(false);
                    regionalRepository.save(local);
                }
            }

            log.info("Sincronização concluída com sucesso.");

        } catch (Exception e) {
            log.error("Erro ao sincronizar regionais: {}", e.getMessage());
        }
    }

    private void createNewRegional(ExternalRegionalDTO dto) {
        log.info("Nova regional encontrada: {} - {}", dto.getId(), dto.getNome());
        Regional newRegional = Regional.builder()
                .id(dto.getId())
                .name(dto.getNome())
                .active(true)
                .build();
        regionalRepository.save(newRegional);
    }
}