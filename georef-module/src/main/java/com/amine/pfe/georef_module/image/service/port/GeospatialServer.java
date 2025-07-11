package com.amine.pfe.georef_module.image.service.port;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.amine.pfe.georef_module.enums.GeorefSettings;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;

public interface GeospatialServer {
    public InputStream processGeoref(File imageFile, List<GcpDto> gcps, GeorefSettings settings);
    public boolean deleteGeorefFile(String filename);
}
