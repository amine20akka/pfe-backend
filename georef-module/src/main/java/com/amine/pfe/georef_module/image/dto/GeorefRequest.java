package com.amine.pfe.georef_module.image.dto;

import java.util.List;

import com.amine.pfe.georef_module.enums.GeorefSettings;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;

import lombok.Data;

@Data
public class GeorefRequest {
    private GeorefSettings georefSettings;
    private List<GcpDto> gcps;
}