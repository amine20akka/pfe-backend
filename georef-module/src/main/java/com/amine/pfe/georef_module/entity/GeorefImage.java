package com.amine.pfe.georef_module.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.amine.pfe.georef_module.enums.Compression;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.enums.ResamplingMethod;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;

@Entity
@Table(name = "georef_images", schema = "georef")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeorefImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private String filepathOriginal;

    private String filepathGeoreferenced;

    private String outputFilename;

    private LocalDateTime uploadingDate;

    private LocalDateTime lastGeoreferencingDate;

    @Enumerated(EnumType.STRING)
    private TransformationType transformationType;

    @Enumerated(EnumType.STRING)
    private Srid srid;

    @Enumerated(EnumType.STRING)
    private GeorefStatus status;

    @Enumerated(EnumType.STRING)
    private ResamplingMethod resamplingMethod;

    @Enumerated(EnumType.STRING)
    private Compression compression;

    private Double meanResidual;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gcp> gcps;

    @OneToOne(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private GeorefLayer layer;
}