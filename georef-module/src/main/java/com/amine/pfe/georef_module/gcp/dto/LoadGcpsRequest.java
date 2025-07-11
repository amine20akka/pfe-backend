package com.amine.pfe.georef_module.gcp.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class LoadGcpsRequest {
    private UUID imageId;
    private List<GcpDto> gcps;
    private boolean overwrite;
}
