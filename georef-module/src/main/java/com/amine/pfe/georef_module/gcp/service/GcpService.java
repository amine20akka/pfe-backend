package com.amine.pfe.georef_module.gcp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.LoadGcpsRequest;
import com.amine.pfe.georef_module.gcp.dto.ResidualsRequest;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResponse;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;
import com.amine.pfe.georef_module.gcp.exceptions.DuplicateGcpIndexException;
import com.amine.pfe.georef_module.gcp.exceptions.GcpNotFoundException;
import com.amine.pfe.georef_module.gcp.mapper.GcpMapper;
import com.amine.pfe.georef_module.gcp.repository.GcpRepository;
import com.amine.pfe.georef_module.gcp.service.port.GcpFactory;
import com.amine.pfe.georef_module.gcp.service.port.ResidualsService;
import com.amine.pfe.georef_module.image.repository.GeorefImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GcpService {

        private final GcpRepository gcpRepository;
        private final GeorefImageRepository imageRepository;
        private final GcpFactory gcpFactory;
        private final ResidualsService residualsService;

        public GcpDto addGcp(GcpDto gcpDto) {

                UUID imageId = gcpDto.getImageId();
                validateImageIdNotNull(imageId);

                GeorefImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> {
                                        return new ImageNotFoundException(
                                                        "Image avec l'ID " + imageId + " introuvable.");
                                });

                Integer nextIndex = gcpRepository.findMaxIndexByImageId(imageId)
                                .map(maxIndex -> maxIndex + 1)
                                .orElse(1);

                if (gcpRepository.existsByImageIdAndIndex(imageId, nextIndex)) {
                        throw new DuplicateGcpIndexException("Un GCP avec ce même index existe déjà pour cette image.");
                }

                Gcp gcp = gcpFactory.createGcp(image,
                                gcpDto.getSourceX(),
                                gcpDto.getSourceY(),
                                gcpDto.getMapX(),
                                gcpDto.getMapY(),
                                nextIndex);

                Gcp saved = gcpRepository.save(gcp);
                return GcpMapper.toDto(saved);
        }

        public List<GcpDto> getGcpsByImageId(UUID imageId) {

                validateImageIdNotNull(imageId);

                GeorefImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> {
                                        return new ImageNotFoundException(
                                                        "Image avec l'ID " + imageId + " introuvable.");
                                });

                List<Gcp> gcps = gcpRepository.findByImageId(image.getId());
                return GcpMapper.toGcpDtoList(gcps);
        }

        @Transactional
        public List<GcpDto> deleteGcpById(UUID gcpId) {
                Gcp gcpToDelete = gcpRepository.findById(gcpId)
                                .orElseThrow(() -> new GcpNotFoundException("GCP non trouvé avec l'id : " + gcpId));

                UUID imageId = gcpToDelete.getImage().getId();
                int indexToDelete = gcpToDelete.getIndex();

                gcpRepository.delete(gcpToDelete);

                List<Gcp> remainingGcps = gcpRepository.findAllByImageIdOrderByIndex(imageId);

                if (indexToDelete < remainingGcps.size() + 1) {
                        for (int i = 0; i < remainingGcps.size(); i++) {
                                remainingGcps.get(i).setIndex(i + 1);
                        }
                        remainingGcps = gcpRepository.saveAll(remainingGcps);
                }

                return GcpMapper.toGcpDtoList(remainingGcps);
        }

        @Transactional
        public GcpDto updateGcp(GcpDto gcpDto) {
                if (gcpDto.getId() == null) {
                        throw new IllegalArgumentException("GCP ID cannot be null.");
                }

                Gcp gcpToUpdate = gcpRepository.findById(gcpDto.getId())
                                .orElseThrow(() -> new GcpNotFoundException(
                                                "GCP not found : " + gcpDto.getId()));

                gcpToUpdate.setSourceX(gcpDto.getSourceX());
                gcpToUpdate.setSourceY(gcpDto.getSourceY());
                gcpToUpdate.setMapX(gcpDto.getMapX());
                gcpToUpdate.setMapY(gcpDto.getMapY());

                return GcpMapper.toDto(gcpToUpdate);
        }

        @Transactional
        public ResidualsResponse updateResiduals(ResidualsRequest residualsRequest) {
                validateImageIdNotNull(residualsRequest.getImageId());

                GeorefImage image = imageRepository.findById(residualsRequest.getImageId())
                                .orElseThrow(() -> new ImageNotFoundException(
                                                "Image avec l'ID " + residualsRequest.getImageId() + " introuvable."));

                List<Gcp> gcps = getGcpsForImage(residualsRequest.getImageId());

                List<GcpDto> gcpDtos = GcpMapper.toGcpDtoList(gcps);

                int minPointsRequired = residualsService.getMinimumPointsRequired(residualsRequest.getType());

                if (!residualsService.hasEnoughGCPs(gcpDtos, residualsRequest.getType())) {
                        List<Gcp> clearedGcps = clearResiduals(gcps);
                        return buildResponse(false, clearedGcps, null, minPointsRequired);
                }

                ResidualsResult result = residualsService.computeResiduals(
                                gcpDtos,
                                residualsRequest.getType(),
                                residualsRequest.getSrid());

                List<Gcp> updatedGcps = updateResidualsInGcps(gcps, result.getResiduals());
                updateMeanResidualForImage(image, result.getRmse());

                return buildResponse(true, updatedGcps, result.getRmse(), minPointsRequired);
        }

        @Transactional
        public List<GcpDto> loadGcps(LoadGcpsRequest request) {
                UUID imageId = request.getImageId();
                validateImageIdNotNull(imageId);

                if (request.getGcps() == null || request.getGcps().isEmpty()) {
                        throw new IllegalArgumentException("La liste des GCPs ne peut pas être vide.");
                }

                GeorefImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> new ImageNotFoundException(
                                                "Image avec l'ID " + imageId + " introuvable."));

                if (request.isOverwrite()) {
                        gcpRepository.deleteByImageId(imageId);
                }

                Integer startIndex = gcpRepository.findMaxIndexByImageId(imageId)
                                .map(maxIndex -> maxIndex + 1)
                                .orElse(1);

                List<Gcp> newGcps = new ArrayList<>();
                Integer index = startIndex;

                for (GcpDto dto : request.getGcps()) {
                        if (gcpRepository.existsByImageIdAndIndex(imageId, index)) {
                                throw new DuplicateGcpIndexException("Un GCP avec l'index " + index + " existe déjà.");
                        }

                        Gcp gcp = gcpFactory.createGcp(
                                        image,
                                        dto.getSourceX(),
                                        dto.getSourceY(),
                                        dto.getMapX(),
                                        dto.getMapY(),
                                        index++);
                        newGcps.add(gcp);
                }

                List<Gcp> savedGcps = gcpRepository.saveAll(newGcps);
                return GcpMapper.toGcpDtoList(savedGcps);
        }

        @Transactional
        public boolean deleteAllGcpsByImageId(UUID imageId) {
                List<Gcp> gcpList = gcpRepository.findAllByImageId(imageId);

                if (gcpList.isEmpty()) {
                        return false;
                }

                gcpRepository.deleteAll(gcpList);
                return true;
        }

        private void validateImageIdNotNull(UUID imageId) {
                if (imageId == null) {
                        throw new IllegalArgumentException("L'ID de l'image ne peut pas être null.");
                }
        }

        private List<Gcp> getGcpsForImage(UUID imageId) {
                List<Gcp> gcps = gcpRepository.findByImageId(imageId);
                if (gcps.isEmpty()) {
                        throw new GcpNotFoundException("Aucun GCP trouvé pour l'image avec l'ID : " + imageId);
                }
                return gcps;
        }

        private List<Gcp> clearResiduals(List<Gcp> gcps) {
                gcps.forEach(gcp -> gcp.setResidual(null));
                List<Gcp> updatedGcps = gcpRepository.saveAll(gcps);
                return updatedGcps;
        }

        private List<Gcp> updateResidualsInGcps(List<Gcp> gcps, List<Double> residuals) {
                if (gcps.size() != residuals.size()) {
                        throw new IllegalStateException("Number of residuals does not match number of GCPs.");
                }
                for (int i = 0; i < gcps.size(); i++) {
                        double residual = Math.round(residuals.get(i) * 10000.0) / 10000.0;
                        gcps.get(i).setResidual(residual);
                }
                List<Gcp> updatedGcps = gcpRepository.saveAll(gcps);
                return updatedGcps;
        }

        private void updateMeanResidualForImage(GeorefImage image, double meanResidual) {
                double roundedMeanResidual = Math.round(meanResidual * 10000.0) / 10000.0;
                image.setMeanResidual(roundedMeanResidual);
        }

        private ResidualsResponse buildResponse(boolean success, List<Gcp> gcps, Double rmse, int minRequired) {
                List<GcpDto> gcpDtos = GcpMapper.toGcpDtoList(gcps);
                return new ResidualsResponse(success, gcpDtos, rmse, minRequired);
        }
}
