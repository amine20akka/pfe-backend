package com.amine.pfe.georef_module.gcp.dto;

import java.util.UUID;

import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;

import lombok.Data;

@Data
public class ResidualsRequest {
    private UUID imageId;
    private TransformationType type;
    private Srid srid;
}