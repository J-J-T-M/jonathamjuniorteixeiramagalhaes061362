package com.jonathamjtm.gestaoartistas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArtistRequest {

    @NotBlank(message = "O nome do artista é obrigatório")
    @Schema(description = "Nome do artista ou banda", example = "Legião Urbana")
    private String name;
}