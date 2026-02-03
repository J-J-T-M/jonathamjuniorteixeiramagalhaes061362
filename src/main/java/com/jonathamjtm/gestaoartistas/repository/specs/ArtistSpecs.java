package com.jonathamjtm.gestaoartistas.repository.specs;

import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.entity.ArtistType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ArtistSpecs {

    public static Specification<Artist> nameContainsIgnoreCase(String name) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Artist> createdAfter(LocalDateTime date) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Artist> hasType(ArtistType type) {
        return (root, query, builder) ->
                builder.equal(root.get("type"), type);
    }
}