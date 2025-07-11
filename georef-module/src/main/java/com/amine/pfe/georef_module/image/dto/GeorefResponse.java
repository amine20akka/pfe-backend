package com.amine.pfe.georef_module.image.dto;

import java.time.LocalDateTime;

import com.amine.pfe.georef_module.enums.GeorefStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeorefResponse {
    private boolean enoughGCPs;
    private int minPointsRequired;
    private String message;
    private GeorefStatus status;
    private LocalDateTime lastGeoreferencingDate;
    private GeorefLayerDto georefLayer;

    public GeorefResponse(boolean value, int nbPoints, String msg) {
        enoughGCPs = value;
        minPointsRequired = nbPoints;
        message = msg;
    }
}