package com.jonathamjtm.gestaoartistas.dto.response;

import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {

    private Long id;
    private String title;
    private Integer releaseYear;
    private List<Long> artistIds;

    public AlbumResponse(Album album) {
        this.id = album.getId();
        this.title = album.getTitle();
        this.releaseYear = album.getReleaseYear();
        if (album.getArtists() != null) {
            this.artistIds = album.getArtists().stream()
                    .map(Artist::getId)
                    .toList();
        }
    }
}