package com.jonathamjtm.gestaoartistas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AlbumRequest {

    @NotBlank
    @Schema(description = "Título do álbum", example = "Hybrid Theory")
    private String title;

    @NotNull
    @Schema(description = "Ano de lançamento", example = "2000")
    private Integer releaseYear;

    @NotEmpty
    @Schema(description = "IDs dos artistas que participaram", example = "[1]")
    private List<Long> artistIds;
}