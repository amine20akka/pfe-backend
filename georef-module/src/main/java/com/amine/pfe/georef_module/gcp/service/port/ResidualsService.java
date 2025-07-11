package com.amine.pfe.georef_module.gcp.service.port;

import java.util.List;

import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;

public interface ResidualsService {
    public ResidualsResult computeResiduals(List<GcpDto> gcps, TransformationType Type, Srid srid);
    public int getMinimumPointsRequired(TransformationType transformationType);
    public boolean hasEnoughGCPs(List<GcpDto> gcps, TransformationType type);
}
