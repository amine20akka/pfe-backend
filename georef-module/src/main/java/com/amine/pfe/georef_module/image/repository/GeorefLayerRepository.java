package com.amine.pfe.georef_module.image.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amine.pfe.georef_module.entity.GeorefLayer;

public interface GeorefLayerRepository extends JpaRepository<GeorefLayer, UUID> {
    Optional<GeorefLayer> findByImageId(UUID imageId);
}
