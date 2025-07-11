package com.amine.pfe.georef_module.gcp.service.impl;

import org.springframework.stereotype.Component;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.gcp.service.port.GcpFactory;

@Component
public class GcpFactoryImpl implements GcpFactory {
    
    @Override
    public Gcp createGcp(GeorefImage image, Double sourceX, Double sourceY, double mapX, double mapY, Integer index) {
        Gcp gcp = new Gcp();
        gcp.setImage(image);
        gcp.setIndex(index);
        gcp.setSourceX(sourceX);
        gcp.setSourceY(sourceY);
        gcp.setMapX(mapX);
        gcp.setMapY(mapY);
        return gcp;
    }
}
