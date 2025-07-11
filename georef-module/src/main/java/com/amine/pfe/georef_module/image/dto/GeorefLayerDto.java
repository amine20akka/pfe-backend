package com.amine.pfe.georef_module.image.dto;

import java.util.UUID;

import com.amine.pfe.georef_module.enums.LayerStatus;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GeorefLayerDto {
    private UUID id;
    private UUID imageId;
    private String workspace;
    private String storeName;
    private String layerName;
    private String wmsUrl;
    private LayerStatus status;
}
