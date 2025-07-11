package com.amine.pfe.georef_module.image.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amine.pfe.georef_module.entity.GeorefImage;

public interface GeorefImageRepository extends JpaRepository<GeorefImage, UUID> {
    boolean existsByHash(String hash);
    GeorefImage findByHash(String hash);
}