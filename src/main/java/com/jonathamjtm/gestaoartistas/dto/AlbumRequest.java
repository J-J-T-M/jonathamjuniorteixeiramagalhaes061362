package com.jonathamjtm.gestaoartistas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AlbumRequest {

    @NotBlank(message = "O título do álbum é obrigatório")
    @Schema(example = "As Quatro Estações")
    private String title;

    @NotNull(message = "O ano de lançamento é obrigatório")
    @Schema(example = "1989")
    private Integer releaseYear;

    @NotEmpty(message = "O álbum deve ter pelo menos um artista")
    @Schema(description = "Lista de IDs dos artistas desse álbum", example = "[1, 2]")
    private List<Long> artistIds;
}