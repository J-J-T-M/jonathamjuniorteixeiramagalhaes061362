package com.jonathamjtm.gestaoartistas.repository.specs;

import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class AlbumSpecs {

    public static Specification<Album> filterBy(String title, Long artistId, Integer releaseYear, LocalDateTime createdAfter) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();

            // 1. Título (LIKE)
            if (StringUtils.hasText(title)) {
                predicate = builder.and(predicate,
                        builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            // 2. ID do Artista (JOIN)
            if (artistId != null) {
                Join<Album, Artist> artistJoin = root.join("artists");
                predicate = builder.and(predicate, builder.equal(artistJoin.get("id"), artistId));
            }

            // 3. Ano de Lançamento (A partir de...)
            if (releaseYear != null) {
                predicate = builder.and(predicate,
                        builder.greaterThanOrEqualTo(root.get("releaseYear"), releaseYear));
            }

            // 4. Data de Criação (A partir de...)
            if (createdAfter != null) {
                predicate = builder.and(predicate,
                        builder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
            }

            return predicate;
        };
    }
}