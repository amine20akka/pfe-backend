package com.amine.pfe.georef_module.gcp.dto;

import java.util.UUID;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GcpDto {
    private UUID id;
    private UUID imageId;
    private Double sourceX;
    private Double sourceY;
    private Double mapX;
    private Double mapY;
    private int index;
    private Double residual;

    public GcpDto(Double sourceX, Double sourceY, Double mapX, Double mapY) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.mapX = mapX;
        this.mapY = mapY;
    }
}
