package com.amine.pfe.georef_module.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder @AllArgsConstructor
public class PublicationResponse {
    private String workspace;
    private String storeName;
    private String layerName;
    private String wmsUrl;
}
