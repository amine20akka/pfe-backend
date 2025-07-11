package com.amine.pfe.georef_module.enums;

import lombok.Data;

@Data
public class GeorefSettings {

    private String outputFilename;
    private TransformationType transformationType;
    private Srid srid;
    private ResamplingMethod resamplingMethod;
    private Compression compressionType;

}
