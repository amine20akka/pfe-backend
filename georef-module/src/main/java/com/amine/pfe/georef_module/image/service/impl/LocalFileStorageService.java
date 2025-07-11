package com.amine.pfe.georef_module.image.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amine.pfe.georef_module.config.StorageConfig;
import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.image.service.port.FileStorageService;

@Component
public class LocalFileStorageService implements FileStorageService {

    private final StorageConfig storageConfig;

    public LocalFileStorageService(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Override
    public Path existsInOriginalDir(String filenameWithHash) {
        if (filenameWithHash == null) {
            return null;
        }

        Path originalDir = storageConfig.getOriginalDir();
        if (originalDir == null) {
            throw new IllegalStateException("Original directory is not configured");
        }

        Path targetPath = originalDir.resolve(filenameWithHash);

        return targetPath;
    }

    @Override
    public Path existsInGeorefDir(String GeorefFilenameWithHash) {
        if (GeorefFilenameWithHash == null) {
            return null;
        }

        Path georefDir = storageConfig.getGeoreferencedDir();
        if (georefDir == null) {
            throw new IllegalStateException("Georef directory is not configured");
        }

        Path targetPath = georefDir.resolve(GeorefFilenameWithHash);

        return targetPath;
    }

    @Override
    public Path saveOriginalFile(MultipartFile file, String filenameWithHash) throws IOException {
        Path targetPath = storageConfig.getOriginalDir().resolve(filenameWithHash);
        if (!Files.exists(targetPath)) {
            Files.copy(file.getInputStream(), targetPath);
        }
        return targetPath;
    }

    @Override
    public Path getOriginalFilePath(String filename) throws IOException {
        Path targetPath = storageConfig.getOriginalDir().resolve(filename);
        if (!Files.exists(targetPath)) {
            throw new IOException("File not found: " + filename);
        }
        return targetPath;
    }

    @Override
    public void deleteFileByFullPath(String fullPath) throws IOException {
        Path targetPath = Paths.get(fullPath);
        if (Files.exists(targetPath)) {
            Files.delete(targetPath);
        } else {
            throw new ImageNotFoundException("File not found: " + fullPath);
        }
    }

    @Override
    public File getFileByFilePath(String filePath) {
        Path path = Paths.get(filePath);
        return path.toFile();
    }

    @Override
    public String removeHashFromFilePath(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        String filename = path.getFileName().toString();
        String[] parts = filename.split("_", 2);
        if (parts.length < 2) {
            throw new IOException("Le nom de fichier ne contient pas de hash : " + filename);
        }
        return parts[1];
    }

    @Override
    public MediaType detectMediaType(String filename) {
        if (filename.endsWith(".png"))
            return MediaType.IMAGE_PNG;
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @Override
    public Path saveGeoreferencedFile(InputStream inputStream, String GeorefFilenameWithHash) throws IOException {
        Path targetPath = storageConfig.getGeoreferencedDir().resolve(GeorefFilenameWithHash);

        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return targetPath;
    }
}
