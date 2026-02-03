package com.jonathamjtm.gestaoartistas.repository.specs;

import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class AlbumSpecs {

    public static Specification<Album> titleContainsIgnoreCase(String title) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Album> releaseYearGreaterThanEqual(Integer year) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("releaseYear"), year);
    }

    public static Specification<Album> hasArtistId(Long artistId) {
        return (root, query, builder) -> {
            Join<Album, Artist> artists = root.join("artists");
            return builder.equal(artists.get("id"), artistId);
        };
    }
}