package com.amine.pfe.georef_module.image.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.entity.GeorefLayer;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.enums.LayerStatus;
import com.amine.pfe.georef_module.exception.CartographicServerException;
import com.amine.pfe.georef_module.exception.GeorefLayerNotFoundException;
import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.LoadGcpsRequest;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;
import com.amine.pfe.georef_module.gcp.mapper.GcpMapper;
import com.amine.pfe.georef_module.gcp.service.GcpService;
import com.amine.pfe.georef_module.gcp.service.port.ResidualsService;
import com.amine.pfe.georef_module.image.dto.GeorefLayerDto;
import com.amine.pfe.georef_module.image.dto.GeorefRequest;
import com.amine.pfe.georef_module.image.dto.GeorefResponse;
import com.amine.pfe.georef_module.image.dto.PublicationResponse;
import com.amine.pfe.georef_module.image.dto.RegeorefResponse;
import com.amine.pfe.georef_module.image.mapper.ImageMapper;
import com.amine.pfe.georef_module.image.repository.GeorefImageRepository;
import com.amine.pfe.georef_module.image.repository.GeorefLayerRepository;
import com.amine.pfe.georef_module.image.service.port.CartographicServer;
import com.amine.pfe.georef_module.image.service.port.FileStorageService;
import com.amine.pfe.georef_module.image.service.port.GeospatialServer;
import com.amine.pfe.georef_module.image.util.FileUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeorefLayerService {

    private final GeorefLayerRepository georefLayerRepository;
    private final GeorefImageRepository georefImageRepository;
    private final GeospatialServer geospatialServer;
    private final CartographicServer cartographicServer;
    private final ResidualsService residualsService;
    private final FileStorageService fileStorageService;
    private final GeorefImageService georefImageService;
    private final GcpService gcpService;

    @Transactional
    public GeorefResponse georeferenceImage(GeorefRequest request, UUID imageId) throws IOException {

        int minPointsRequired = residualsService
                .getMinimumPointsRequired(request.getGeorefSettings().getTransformationType());

        if (!residualsService.hasEnoughGCPs(request.getGcps(), request.getGeorefSettings().getTransformationType())) {
            String message = "";
            switch (request.getGeorefSettings().getTransformationType()) {
                case POLYNOMIALE_1:
                    message = request.getGeorefSettings().getTransformationType().getLabel()
                            + " : Au moins 3 points de contrôle requis";
                    break;
                case POLYNOMIALE_2:
                    message = request.getGeorefSettings().getTransformationType().getLabel()
                            + " : Au moins 6 points de contrôle requis";
                    break;
                case POLYNOMIALE_3:
                    message = request.getGeorefSettings().getTransformationType().getLabel()
                            + " : Au moins 10 points de contrôle requis";
                    break;
                default:
                    break;
            }
            return new GeorefResponse(false, minPointsRequired, message);
        }

        GeorefImage image = georefImageRepository.findById(imageId)
                .orElseThrow(() -> {
                    return new ImageNotFoundException("Image avec l'ID " + imageId + " non trouvée.");
                });

        try {
            File originalImage = fileStorageService.getFileByFilePath(image.getFilepathOriginal());

            image.setStatus(GeorefStatus.PROCESSING);
            georefImageRepository.save(image);

            InputStream georefInputStream = geospatialServer.processGeoref(
                    originalImage,
                    request.getGcps(),
                    request.getGeorefSettings());

            String georefFilenameWithHash = image.getHash() + "_" + request.getGeorefSettings().getOutputFilename();

            Path georeferencedPath = fileStorageService.saveGeoreferencedFile(
                    georefInputStream,
                    georefFilenameWithHash);

            ResidualsResult result = residualsService.computeResiduals(
                    request.getGcps(),
                    request.getGeorefSettings().getTransformationType(),
                    request.getGeorefSettings().getSrid());

            image.setMeanResidual(result.getRmse());
            gcpService.loadGcps(new LoadGcpsRequest(imageId, request.getGcps(), true));
            image.setCompression(request.getGeorefSettings().getCompressionType());
            image.setResamplingMethod(request.getGeorefSettings().getResamplingMethod());
            image.setSrid(request.getGeorefSettings().getSrid());
            image.setTransformationType(request.getGeorefSettings().getTransformationType());
            image.setOutputFilename(request.getGeorefSettings().getOutputFilename());
            image.setFilepathGeoreferenced(georeferencedPath.toString());
            image.setStatus(GeorefStatus.COMPLETED);
            image.setLastGeoreferencingDate(LocalDateTime.now());

            georefImageRepository.save(image);

            String normalizedLayerName = FileUtils.normalizeLayerName(request.getGeorefSettings().getOutputFilename());
            PublicationResponse response = cartographicServer.publishGeoTiff(georeferencedPath.toString(),
                    normalizedLayerName, normalizedLayerName);

            if (response == null) {
                throw new CartographicServerException("Null response from Georef Layer Publication");
            }

            GeorefLayer layer = new GeorefLayer();
            layer.setImage(image);
            layer.setWorkspace(response.getWorkspace());
            layer.setStoreName(response.getStoreName());
            layer.setLayerName(response.getLayerName());
            layer.setWmsUrl(response.getWmsUrl());
            layer.setStatus(LayerStatus.PUBLISHED);

            georefLayerRepository.save(layer);

            GeorefResponse georefResponse = new GeorefResponse(
                    true,
                    0,
                    "",
                    image.getStatus(),
                    image.getLastGeoreferencingDate(),
                    ImageMapper.toDto(layer));

            return georefResponse;

        } catch (IOException e) {
            log.error("Erreur IO lors du géoréférencement de l'image ID: {}", imageId, e);
            image.setStatus(GeorefStatus.FAILED);
            georefImageRepository.save(image);
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors du géoréférencement de l'image ID: {}", imageId, e);
            image.setStatus(GeorefStatus.FAILED);
            georefImageRepository.save(image);
            throw e;
        }
    }

    @Transactional
    public void deleteGeorefLayerById(UUID georefLayerId) throws IOException {
        GeorefLayer layer = georefLayerRepository.findById(georefLayerId)
                .orElseThrow(() -> new GeorefLayerNotFoundException(
                        "Couche géoréférencée avec l'ID " + georefLayerId + " non trouvée."));

        GeorefImage image = layer.getImage();

        boolean deleted = cartographicServer.deleteGeoTiffLayer(layer.getLayerName(), layer.getStoreName());
        if (!deleted) {
            throw new CartographicServerException(
                    "Erreur lors de la suppression de la couche depuis le serveur cartographique");
        }

        image.setLayer(null);
        georefLayerRepository.delete(layer);
        geospatialServer.deleteGeorefFile(image.getOutputFilename());
        fileStorageService.deleteFileByFullPath(image.getFilepathGeoreferenced());
    }

    @Transactional
    public void deleteGeorefLayerAndImageById(UUID georefLayerId) throws IOException {
        GeorefLayer layer = georefLayerRepository.findById(georefLayerId)
                .orElseThrow(() -> new GeorefLayerNotFoundException(
                        "Couche géoréférencée avec l'ID " + georefLayerId + " non trouvée."));

        GeorefImage image = layer.getImage();

        if (image.getStatus() != GeorefStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Impossible de supprimer la couche car l'image n'est pas en état COMPLETED.");
        }

        boolean deleted = cartographicServer.deleteGeoTiffLayer(layer.getLayerName(), layer.getStoreName());
        if (deleted) {
            georefImageService.deleteImageById(image.getId());
            geospatialServer.deleteGeorefFile(image.getOutputFilename());
            fileStorageService.deleteFileByFullPath(image.getFilepathGeoreferenced());
        } else {
            throw new CartographicServerException(
                    "Erreur lors de la suppression de la couche depuis le serveur cartographique");
        }
    }

    public List<GeorefLayerDto> getAllGeorefLayers() {
        List<GeorefLayer> georefLayers = georefLayerRepository.findAll();
        return ImageMapper.toDtoList(georefLayers);
    }

    @Transactional
    public RegeorefResponse prepareRegeoref(UUID imageId) {
        if (imageId == null) {
            throw new IllegalArgumentException("L'ID de l'image ne peut pas être null");
        }

        final GeorefImage sourceImage = georefImageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image avec l'ID " + imageId + " non trouvée"));

        final GeorefImage regeorefImage = createRegeorefImageFrom(sourceImage);
        final GeorefImage savedImage = georefImageRepository.save(regeorefImage);

        copyGcpsToNewImage(sourceImage.getGcps(), savedImage.getId());

        List<GcpDto> regeorefImageGcps = gcpService.getGcpsByImageId(savedImage.getId());

        return new RegeorefResponse(ImageMapper.toDto(savedImage), regeorefImageGcps);
    }

    private GeorefImage createRegeorefImageFrom(GeorefImage sourceImage) {
        GeorefImage regeorefImage = new GeorefImage();
        regeorefImage.setHash(sourceImage.getHash());
        regeorefImage.setFilepathOriginal(sourceImage.getFilepathOriginal());
        regeorefImage.setCompression(sourceImage.getCompression());
        regeorefImage.setOutputFilename(sourceImage.getOutputFilename());
        regeorefImage.setResamplingMethod(sourceImage.getResamplingMethod());
        regeorefImage.setSrid(sourceImage.getSrid());
        regeorefImage.setTransformationType(sourceImage.getTransformationType());

        return regeorefImage;
    }

    private void copyGcpsToNewImage(List<Gcp> sourceGcps, UUID targetImageId) {
        if (sourceGcps == null || sourceGcps.isEmpty()) {
            return;
        }

        List<GcpDto> gcpDtos = GcpMapper.toGcpDtoList(sourceGcps);

        gcpDtos.forEach(gcpDto -> {
            gcpDto.setImageId(targetImageId);
            gcpService.addGcp(gcpDto);
        });
    }

}
