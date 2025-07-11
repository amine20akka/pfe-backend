package com.amine.pfe.georef_module.image.service.impl;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.enums.Compression;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.enums.ResamplingMethod;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
import com.amine.pfe.georef_module.image.service.port.GeorefImageFactory;
import com.amine.pfe.georef_module.image.util.FileUtils;

@Component
public class GeorefImageFactoryImpl implements GeorefImageFactory {
    @Override
    public GeorefImage create(String hash, Path path, String originalFilename) {
        String outputFilename = FileUtils.normalizeOutputFilename(originalFilename, originalFilename);

        GeorefImage image = new GeorefImage();
        image.setHash(hash);
        image.setFilepathOriginal(path.toString());
        image.setUploadingDate(LocalDateTime.now());
        image.setStatus(GeorefStatus.UPLOADED);
        image.setCompression(Compression.getDefault());
        image.setResamplingMethod(ResamplingMethod.getDefault());
        image.setSrid(Srid.getDefault());
        image.setTransformationType(TransformationType.getDefault());
        image.setOutputFilename(outputFilename);
        return image;
    }
}

