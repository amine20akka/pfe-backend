package com.amine.pfe.georef_module.image.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.image.dto.GeorefImageDto;
import com.amine.pfe.georef_module.image.exceptions.ImageAlreadyGeoreferencedException;
import com.amine.pfe.georef_module.image.exceptions.UnsupportedImageFormatException;
import com.amine.pfe.georef_module.image.mapper.ImageMapper;
import com.amine.pfe.georef_module.image.repository.GeorefImageRepository;
import com.amine.pfe.georef_module.image.service.port.FileStorageService;
import com.amine.pfe.georef_module.image.service.port.GeorefImageFactory;
import com.amine.pfe.georef_module.image.service.port.HashCalculator;
import com.amine.pfe.georef_module.image.util.FileUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GeorefImageService {

    private final GeorefImageRepository repository;
    private final HashCalculator hashCalculator;
    private final FileStorageService fileStorageService;
    private final GeorefImageFactory imageFactory;

    public GeorefImageDto uploadImage(MultipartFile file) throws IOException {
        String mimeType = file.getContentType();
        if (mimeType == null || !FileStorageService.SUPPORTED_MIME_TYPES.contains(mimeType)) {
            throw new UnsupportedImageFormatException("Format non supporte : " + mimeType);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nom de fichier manquant dans l'image importee.");
        }

        String hash = hashCalculator.calculate(file);

        if (repository.existsByHash(hash)) {
            GeorefImage existingImage = repository.findByHash(hash);

            if (existingImage.getStatus() == GeorefStatus.COMPLETED) {
                throw new ImageAlreadyGeoreferencedException(
                        "L'image est déjà géoréférencée.");
            } else {
                GeorefImageDto savedDto = ImageMapper.toDto(existingImage);
                savedDto.setFilepathOriginal(originalFilename);
                return savedDto;
            }
        }

        String filename = hash + "_" + originalFilename;
        Path storedPath = fileStorageService.existsInOriginalDir(filename);
        storedPath = fileStorageService.saveOriginalFile(file, filename);

        GeorefImage image = imageFactory.create(hash, storedPath, originalFilename);
        GeorefImage saved = repository.save(image);

        GeorefImageDto savedDto = ImageMapper.toDto(saved);
        savedDto.setFilepathOriginal(originalFilename);

        return savedDto;
    }

    public GeorefImageDto updateGeoreferencingParams(GeorefImageDto dto) throws IOException {
        UUID imageId = dto.getId();
        GeorefImage image = repository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image avec l'ID " + imageId + " non trouvée."));

        String normalizedOutputFilename = FileUtils.normalizeOutputFilename(dto.getOutputFilename(),
                dto.getOutputFilename());

        image.setTransformationType(dto.getTransformationType());
        image.setSrid(dto.getSrid());
        image.setResamplingMethod(dto.getResamplingMethod());
        image.setCompression(dto.getCompression());
        image.setOutputFilename(normalizedOutputFilename);

        GeorefImage updated = repository.save(image);
        fileStorageService.removeHashFromFilePath(updated.getFilepathOriginal());
        return ImageMapper.toDto(updated);
    }

    public void deleteImageById(UUID id) throws IOException {
        GeorefImage image = repository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image introuvable avec l'id " + id));

        repository.delete(image);
        fileStorageService.deleteFileByFullPath(image.getFilepathOriginal());
    }

    public void deleteGeorefImageWithoutFile(UUID imageId) {
        GeorefImage image = repository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image introuvable avec l'id " + imageId));

        repository.delete(image);
    }

    public GeorefImageDto getImageById(UUID id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'image ne peut pas être nul.");
        }

        GeorefImage georefImage = repository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with the id : " + id));

        String filename = fileStorageService.removeHashFromFilePath(georefImage.getFilepathOriginal());
        if (filename == null || fileStorageService.existsInOriginalDir(filename) == null) {
            throw new ImageNotFoundException("This image does not exist in the file system.");
        }

        GeorefImageDto georefImageDto = ImageMapper.toDto(georefImage);
        georefImageDto.setFilepathOriginal(filename);
        return georefImageDto;
    }

    public File loadOriginalImageById(UUID id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'image ne peut pas être nul.");
        }

        GeorefImage image = repository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found for id: " + id));

        return fileStorageService.getFileByFilePath(image.getFilepathOriginal());
    }
}
