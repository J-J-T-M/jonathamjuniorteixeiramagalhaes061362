package com.jonathamjtm.gestaoartistas.repository;

import com.jonathamjtm.gestaoartistas.entity.Regional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long> {

    Page<Regional> findByActive(Boolean active, Pageable pageable);

    List<Regional> findByActiveTrue();

    Optional<Regional> findByExternalIdAndActiveTrue(Long externalId);
}