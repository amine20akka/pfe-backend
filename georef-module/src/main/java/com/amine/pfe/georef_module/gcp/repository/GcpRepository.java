package com.amine.pfe.georef_module.gcp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amine.pfe.georef_module.entity.Gcp;

public interface GcpRepository extends JpaRepository<Gcp, UUID> {
    List<Gcp> findByImageId(UUID imageId);

    boolean existsByImageIdAndIndex(UUID imageId, int index);

    List<Gcp> findAllByImageIdOrderByIndex(UUID imageId);

    @Query("SELECT MAX(g.index) FROM Gcp g WHERE g.image.id = :imageId")
    Optional<Integer> findMaxIndexByImageId(@Param("imageId") UUID imageId);

    void deleteByImageId(UUID imageId);

    List<Gcp> findAllByImageId(UUID imageId);
}
