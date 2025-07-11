package com.amine.pfe.georef_module.image.service.port;

import com.amine.pfe.georef_module.image.dto.PublicationResponse;

public interface CartographicServer {
    public PublicationResponse publishGeoTiff(String georeferencedImagePath, String GeotiffFilename, String storeName);
    public boolean deleteGeoTiffLayer(String layerName, String storeName);
}
