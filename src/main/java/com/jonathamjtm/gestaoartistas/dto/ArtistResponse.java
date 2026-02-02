package com.jonathamjtm.gestaoartistas.dto;

import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ArtistResponse {
    private Long id;
    private String name;
    private List<String> albums;

    public ArtistResponse(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();

        if (artist.getAlbums() != null) {
            this.albums = artist.getAlbums().stream()
                    .map(Album::getTitle)
                    .collect(Collectors.toList());
        } else {
            this.albums = Collections.emptyList();
        }
    }
}