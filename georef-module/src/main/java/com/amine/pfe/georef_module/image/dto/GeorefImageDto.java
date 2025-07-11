package com.amine.pfe.georef_module.image.dto;

import com.amine.pfe.georef_module.enums.Compression;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.enums.ResamplingMethod;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeorefImageDto {

    private UUID id;
    private String hash;
    private String filepathOriginal;
    private String filepathGeoreferenced;
    private String outputFilename;
    private LocalDateTime uploadingDate;
    private LocalDateTime lastGeoreferencingDate;
    private TransformationType transformationType;
    private Srid srid;
    private GeorefStatus status;
    private ResamplingMethod resamplingMethod;
    private Compression compression;
    private Double meanResidual;

}
