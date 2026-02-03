package com.jonathamjtm.gestaoartistas.dto.request;

import com.jonathamjtm.gestaoartistas.entity.ArtistType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtistRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome da banda ou artista solo", example = "Serj Tankian")
    private String name;

    @NotNull(message = "O tipo de artista (SINGER ou BAND) é obrigatório")
    @Schema(description = "Tipo de artista: SINGER (Cantor) ou BAND (Banda)", example = "SINGER")
    private ArtistType type;
}