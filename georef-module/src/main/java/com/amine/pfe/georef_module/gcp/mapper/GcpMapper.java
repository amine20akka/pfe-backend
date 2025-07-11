package com.amine.pfe.georef_module.gcp.mapper;

import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class GcpMapper {

    // -------- GCP --------
    public static GcpDto toDto(Gcp entity) {
        return GcpDto.builder()
                .id(entity.getId())
                .imageId(entity.getImage().getId())
                .sourceX(entity.getSourceX())
                .sourceY(entity.getSourceY())
                .mapX(entity.getMapX())
                .mapY(entity.getMapY())
                .index(entity.getIndex())
                .residual(entity.getResidual())
                .build();
    }

    public static Gcp toEntity(GcpDto dto, GeorefImage image) {
        Gcp entity = new Gcp();
        entity.setId(dto.getId());
        entity.setImage(image);
        entity.setSourceX(dto.getSourceX());
        entity.setSourceY(dto.getSourceY());
        entity.setMapX(dto.getMapX());
        entity.setMapY(dto.getMapY());
        entity.setIndex(dto.getIndex());
        entity.setResidual(dto.getResidual());
        return entity;
    }

    // -------- Util --------
    public static List<GcpDto> toGcpDtoList(List<Gcp> gcps) {
        return gcps.stream().map(GcpMapper::toDto).collect(Collectors.toList());
    }

    public static List<Gcp> toGcpEntityList(List<GcpDto> gcpDtos, GeorefImage image) {
        return gcpDtos.stream().map(gcpDto -> toEntity(gcpDto, image)).collect(Collectors.toList());
    }
}
