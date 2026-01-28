package com.jonathamjtm.gestaoartistas.repository.specs;

import com.jonathamjtm.gestaoartistas.entity.Artist;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class ArtistSpecs {

    public static Specification<Artist> filterBy(String name, LocalDateTime createdAfter) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();

            // Filtro por Nome (LIKE)
            if (StringUtils.hasText(name)) {
                predicate = builder.and(predicate,
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            // Filtro por Data de Criação (A partir de...)
            if (createdAfter != null) {
                predicate = builder.and(predicate,
                        builder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
            }

            return predicate;
        };
    }
}