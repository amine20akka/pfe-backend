package com.amine.pfe.georef_module.gcp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;

public class GcpMapperTest {

    @Test
    void shouldMapGcpToDtoCorrectly() {
        // GIVEN
        GeorefImage image = new GeorefImage();
        UUID id = UUID.randomUUID();
        Gcp gcp = new Gcp();
        gcp.setId(id);
        gcp.setImage(image);
        gcp.setIndex(3);
        gcp.setSourceX(123.32);
        gcp.setSourceY(789.44);
        gcp.setMapX(234233.332);
        gcp.setMapY(233433.56);
        gcp.setResidual(0.123);

        // WHEN
        GcpDto dto = GcpMapper.toDto(gcp);

        // THEN
        assertEquals(id, dto.getId());
        assertEquals(image.getId(), dto.getImageId());
        assertEquals(3, dto.getIndex());
        assertEquals(123.32, dto.getSourceX());
        assertEquals(789.44, dto.getSourceY());
        assertEquals(234233.332, dto.getMapX());
        assertEquals(233433.56, dto.getMapY());
        assertEquals(0.123, dto.getResidual());
    }

    @Test
    void shouldMapDtotoGcpCorrectly() {
        // GIVEN
        GeorefImage image = new GeorefImage();
        UUID id = UUID.randomUUID();
        GcpDto dto = new GcpDto();
        dto.setId(id);
        dto.setImageId(image.getId());
        dto.setIndex(3);
        dto.setSourceX(123.44);
        dto.setSourceY(789.44);
        dto.setMapX(234233.332);
        dto.setMapY(233433.56);
        dto.setResidual(0.123);

        // WHEN
        Gcp gcp = GcpMapper.toEntity(dto, image);

        // THEN
        assertEquals(id, gcp.getId());
        assertEquals(image, gcp.getImage());
        assertEquals(3, gcp.getIndex());
        assertEquals(123.44, gcp.getSourceX());
        assertEquals(789.44, gcp.getSourceY());
        assertEquals(234233.332, gcp.getMapX());
        assertEquals(233433.56, gcp.getMapY());
        assertEquals(0.123, gcp.getResidual());
    }

}
