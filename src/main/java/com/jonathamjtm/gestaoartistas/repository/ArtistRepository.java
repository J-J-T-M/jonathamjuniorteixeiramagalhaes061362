package com.jonathamjtm.gestaoartistas.repository;

import com.jonathamjtm.gestaoartistas.entity.Artist;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ArtistRepository extends JpaRepository<Artist, Long>, JpaSpecificationExecutor<Artist> {

    List<Artist> findByNameContainingIgnoreCase(String name, Sort sort);
}