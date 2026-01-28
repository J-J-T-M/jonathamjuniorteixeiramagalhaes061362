package com.jonathamjtm.gestaoartistas.repository;

import com.jonathamjtm.gestaoartistas.entity.Regional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionalRepository extends JpaRepository<Regional, Long> {

    List<Regional> findByActiveTrue();

    Page<Regional> findByActive(Boolean active, Pageable pageable);
}