package com.jonathamjtm.gestaoartistas.repository;

import com.jonathamjtm.gestaoartistas.entity.AlbumImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumImageRepository extends JpaRepository<AlbumImage, Long> {
}