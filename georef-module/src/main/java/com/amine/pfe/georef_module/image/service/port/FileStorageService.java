package com.amine.pfe.georef_module.image.service.port;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    public static final List<String> SUPPORTED_MIME_TYPES = List.of(
            "image/png", "image/jpeg", "image/jpg");

    Path existsInOriginalDir(String filename);
    Path existsInGeorefDir(String GeorefFilenameWithHash);
    Path saveOriginalFile(MultipartFile file, String filename) throws IOException;
    Path getOriginalFilePath(String filename) throws IOException;
    void deleteFileByFullPath(String fullPath) throws IOException;
    File getFileByFilePath(String originalFilePath) throws IOException;
    String removeHashFromFilePath(String filePath) throws IOException;
    MediaType detectMediaType(String filename);
    Path saveGeoreferencedFile(InputStream inputStream, String filename) throws IOException;
}
