package com.amine.pfe.georef_module.image.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.amine.pfe.georef_module.config.StorageConfig;
import com.amine.pfe.georef_module.image.service.impl.LocalFileStorageService;

@ExtendWith(MockitoExtension.class)
public class LocalFileStorageServiceTest {

    @InjectMocks
    private LocalFileStorageService localFileStorageService;

    @Mock
    private StorageConfig storageConfig;

    @Test
    @DisplayName("doit retourner un fichier à partir de son chemin")
    void shouldReturnFileFromOriginalFilePath() throws IOException {
        // Given
        byte[] content = "image content".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("file", "mock.jpg", "image/png", content);

        String hash = "1a9e46aa05d390aa48745d4bda80a459e850c4b048af0c0faec37d9a2f080abb";
        String filename = hash.concat("_").concat(file.getOriginalFilename());
        Path fullPath = Paths.get(filename);

        // When
        File resultPath = localFileStorageService.getFileByFilePath(fullPath.toString());

        // Then
        assertEquals(fullPath, resultPath.toPath());
    }

    @Test
    @DisplayName("doit supprimer le hash du nom du fichier")
    void shouldRemoveHashFromFileName() throws IOException {
        // Given
        String filename = "test.png";
        String hash = "b7d47232-9f4b-4450-8c8c-c733aa3f4435";
        String filenameWithHash = hash + "_" + filename;

        // When
        String filenameWithoutHash = localFileStorageService.removeHashFromFilePath(filenameWithHash);

        // Then
        assertEquals(filename, filenameWithoutHash);
    }

    @Test
    @DisplayName("should return the file path from the file name")
    void shouldReturnFilePathFromFileName() {
        // GIVEN
        String filename = "mock.jpg";
        String hash = "1a9e46aa05d390aa48745d4bda80a459e850c4b048af0c0faec37d9a2f080abb";
        String filenameWithHash = hash + "_" + filename;
        Path fullPath = Paths.get("./georef-storage/originals").resolve(filenameWithHash);

        when(storageConfig.getOriginalDir()).thenReturn(Paths.get("./georef-storage/originals"));

        // WHEN
        Path filePath = localFileStorageService.existsInOriginalDir(filenameWithHash);

        // THEN
        assertEquals(fullPath, filePath);
    }
}
