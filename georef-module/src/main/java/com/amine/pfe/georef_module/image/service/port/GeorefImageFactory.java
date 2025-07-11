package com.amine.pfe.georef_module.image.service.port;

import java.nio.file.Path;

import com.amine.pfe.georef_module.entity.GeorefImage;

public interface GeorefImageFactory {
    GeorefImage create(String hash, Path path, String originalFilename);
}
