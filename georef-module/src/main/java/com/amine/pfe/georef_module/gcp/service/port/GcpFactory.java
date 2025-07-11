package com.amine.pfe.georef_module.gcp.service.port;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;

public interface GcpFactory {
    Gcp createGcp(GeorefImage image, Double sourceX, Double sourceY, double mapX, double mapY, Integer index);
}
