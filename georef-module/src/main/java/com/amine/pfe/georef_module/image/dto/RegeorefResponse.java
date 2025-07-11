package com.amine.pfe.georef_module.image.dto;

import java.util.List;

import com.amine.pfe.georef_module.gcp.dto.GcpDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RegeorefResponse {
    GeorefImageDto georefImageDto;
    List<GcpDto> gcpDtos;
}
