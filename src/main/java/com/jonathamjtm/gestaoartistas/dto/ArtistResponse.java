package com.jonathamjtm.gestaoartistas.dto;

import com.jonathamjtm.gestaoartistas.entity.Artist;
import lombok.Data;

@Data
public class ArtistResponse {
    private Long id;
    private String name;

    public ArtistResponse(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();
    }
}