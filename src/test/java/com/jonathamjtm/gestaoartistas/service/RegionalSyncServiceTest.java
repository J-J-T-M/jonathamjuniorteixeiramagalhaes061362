package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.BaseIntegrationTest;
import com.jonathamjtm.gestaoartistas.dto.ExternalRegionalDTO;
import com.jonathamjtm.gestaoartistas.entity.Regional;
import com.jonathamjtm.gestaoartistas.repository.RegionalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = "integration.regionais.url=http://fake-api.com/regionais")
class RegionalSyncServiceTest extends BaseIntegrationTest {

    @Autowired
    private RegionalSyncService service;

    @Autowired
    private RegionalRepository repository;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Deve versionar (Inativar Antigo + Criar Novo) quando o nome mudar")
    void shouldVersionRegionalWhenNameChanges() {
        // ARRANGE
        Regional oldRegional = new Regional();
        oldRegional.setExternalId(100L);
        oldRegional.setName("Cuiaba Old Name");
        oldRegional.setActive(true);
        repository.save(oldRegional);

        ExternalRegionalDTO updatedDto = new ExternalRegionalDTO();
        updatedDto.setId(100L);
        updatedDto.setNome("Cuiaba New Name");

        ExternalRegionalDTO[] apiResponse = new ExternalRegionalDTO[]{updatedDto};

        when(restTemplate.getForObject(eq("http://fake-api.com/regionais"), any()))
                .thenReturn(apiResponse);

        // ACT
        service.syncRegionals();

        // ASSERT
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

            Regional oldInDb = repository.findById(oldRegional.getId()).orElseThrow();

            assertThat(oldInDb.isActive())
                    .as("A regional antiga deveria ter sido inativada")
                    .isFalse();

            assertThat(oldInDb.getName()).isEqualTo("Cuiaba Old Name");

            Regional newInDb = repository.findByExternalIdAndActiveTrue(100L).orElseThrow(() ->
                    new AssertionError("A nova regional ativa não foi encontrada")
            );

            assertThat(newInDb.getName()).isEqualTo("Cuiaba New Name");
            assertThat(newInDb.getId()).isNotEqualTo(oldRegional.getId());
        });
    }
}