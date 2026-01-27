package com.jonathamjtm.gestaoartistas.dto;

import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AlbumResponse {
    private Long id;
    private String title;
    private Integer releaseYear;
    private List<String> artistNames;

    public AlbumResponse(Album album) {
        this.id = album.getId();
        this.title = album.getTitle();
        this.releaseYear = album.getReleaseYear();
        this.artistNames = album.getArtists().stream()
                .map(Artist::getName)
                .collect(Collectors.toList());
    }
}