package com.amine.pfe.georef_module.image.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amine.pfe.georef_module.image.dto.GeorefImageDto;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.enums.Compression;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.enums.ResamplingMethod;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;

public class ImageMapperTest {

    @Test
    void shouldMapGeorefImageToDtoCorrectly() {
        // GIVEN
        UUID id = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(id);
        image.setHash("abc123");
        image.setFilepathOriginal("/originals/test.png");
        image.setFilepathGeoreferenced("/georeferenced/test.tif");
        image.setUploadingDate(LocalDateTime.of(2024, 1, 1, 10, 0));
        image.setLastGeoreferencingDate(LocalDateTime.of(2024, 2, 1, 12, 0));
        image.setTransformationType(TransformationType.POLYNOMIALE_1);
        image.setSrid(Srid._3857);
        image.setStatus(GeorefStatus.UPLOADED);
        image.setResamplingMethod(ResamplingMethod.BILINEAR);
        image.setCompression(Compression.LZW);
        image.setMeanResidual(0.5);

        // WHEN
        GeorefImageDto dto = ImageMapper.toDto(image);

        // THEN
        assertEquals(id, dto.getId());
        assertEquals("abc123", dto.getHash());
        assertEquals("/originals/test.png", dto.getFilepathOriginal());
        assertEquals("/georeferenced/test.tif", dto.getFilepathGeoreferenced());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getUploadingDate());
        assertEquals(LocalDateTime.of(2024, 2, 1, 12, 0), dto.getLastGeoreferencingDate());
        assertEquals(ResamplingMethod.BILINEAR, dto.getResamplingMethod());
        assertEquals(Srid._3857, dto.getSrid());
        assertEquals(TransformationType.POLYNOMIALE_1, dto.getTransformationType());
        assertEquals(Compression.LZW, dto.getCompression());
        assertEquals(0.5, dto.getMeanResidual());
    }

    @Test
    void shouldMapDtoToGeorefImageCorrectly() {
        // GIVEN
        UUID id = UUID.randomUUID();
        GeorefImageDto dto = GeorefImageDto.builder()
                .id(id)
                .hash("abc123")
                .filepathOriginal("/originals/test.png")
                .filepathGeoreferenced("/georeferenced/test.tif")
                .uploadingDate(LocalDateTime.of(2024, 1, 1, 10, 0))
                .lastGeoreferencingDate(LocalDateTime.of(2024, 2, 1, 12, 0))
                .transformationType(TransformationType.POLYNOMIALE_1)
                .srid(Srid._3857)
                .status(GeorefStatus.UPLOADED)
                .resamplingMethod(ResamplingMethod.NEAREST)
                .compression(Compression.LZW)
                .meanResidual(1.2)
                .build();

        // WHEN
        GeorefImage entity = ImageMapper.toEntity(dto);

        // THEN
        assertEquals(id, entity.getId());
        assertEquals("abc123", entity.getHash());
        assertEquals("/originals/test.png", entity.getFilepathOriginal());
        assertEquals("/georeferenced/test.tif", entity.getFilepathGeoreferenced());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), entity.getUploadingDate());
        assertEquals(LocalDateTime.of(2024, 2, 1, 12, 0), entity.getLastGeoreferencingDate());
        assertEquals(ResamplingMethod.NEAREST, entity.getResamplingMethod());
        assertEquals(Srid._3857, entity.getSrid());
        assertEquals(TransformationType.POLYNOMIALE_1, entity.getTransformationType());
        assertEquals(Compression.LZW, entity.getCompression());
        assertEquals(1.2, entity.getMeanResidual());
        assertEquals(GeorefStatus.UPLOADED, entity.getStatus());
    }
}
