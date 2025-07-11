package com.amine.pfe.georef_module.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gcp", schema = "georef", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "image_id", "index" })
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Gcp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private GeorefImage image;

    @Column(nullable = false)
    private Double sourceX;

    @Column(nullable = false)
    private Double sourceY;

    @Column(nullable = false)
    private Double mapX;

    @Column(nullable = false)
    private Double mapY;

    @Column(nullable = false)
    private int index;

    private Double residual;
}
