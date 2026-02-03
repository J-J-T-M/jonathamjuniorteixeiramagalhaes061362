package com.jonathamjtm.gestaoartistas.dto.response;

import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.entity.ArtistType; // Import do Enum
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponse {

    private Long id;
    private String name;
    private ArtistType type;
    private List<String> albums;
    private LocalDateTime createdAt;

    public ArtistResponse(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();
        this.type = artist.getType();
        this.createdAt = artist.getCreatedAt();

        if (artist.getAlbums() != null) {
            this.albums = artist.getAlbums().stream()
                    .map(Album::getTitle)
                    .collect(Collectors.toList());
        } else {
            this.albums = Collections.emptyList();
        }
    }
}