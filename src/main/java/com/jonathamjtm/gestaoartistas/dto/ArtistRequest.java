package com.jonathamjtm.gestaoartistas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArtistRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome da banda ou artista solo", example = "Linkin Park") // <--- O Pulo do Gato
    private String name;
}