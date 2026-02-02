package com.jonathamjtm.gestaoartistas.repository;

import com.jonathamjtm.gestaoartistas.entity.AlbumImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumImageRepository extends JpaRepository<AlbumImage, Long> {

    Optional<AlbumImage> findByAlbumId(Long albumId);
}