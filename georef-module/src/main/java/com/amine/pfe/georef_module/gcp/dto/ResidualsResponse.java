package com.amine.pfe.georef_module.gcp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ResidualsResponse {
    private boolean success;
    private List<GcpDto> gcpDtos;
    private Double rmse;
    private int minPointsRequired;
}
