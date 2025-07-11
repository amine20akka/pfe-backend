package com.amine.pfe.georef_module.image.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.amine.pfe.georef_module.image.dto.GeorefImageDto;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.image.exceptions.UnsupportedImageFormatException;
import com.amine.pfe.georef_module.image.repository.GeorefImageRepository;
import com.amine.pfe.georef_module.image.service.port.FileStorageService;
import com.amine.pfe.georef_module.image.service.port.GeorefImageFactory;
import com.amine.pfe.georef_module.image.service.port.HashCalculator;

@ExtendWith(MockitoExtension.class)
class GeorefImageServiceTest {

    @InjectMocks
    private GeorefImageService georefImageService;

    @Mock
    private GeorefImageRepository repository;

    @Mock
    private HashCalculator hashCalculator;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private GeorefImageFactory georefImageFactory;

    // Test image non existante : upload + enregistrement
    @Test
    @DisplayName("doit enregistrer une nouvelle image avec succès")
    void shouldUploadNewImageSuccessfully() throws Exception {
        // GIVEN
        byte[] content = "image content".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("file", "mock.jpg", "image/png", content);

        String hash = "1a9e46aa05d390aa48745d4bda80a459e850c4b048af0c0faec37d9a2f080abb";
        String filename = hash.concat("_").concat(file.getOriginalFilename());
        UUID imageId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Path mockPath = Paths.get("georef-storage", "originals", filename);

        GeorefImage imageToSave = new GeorefImage();
        imageToSave.setHash(hash);
        imageToSave.setFilepathOriginal(mockPath.toString());
        imageToSave.setUploadingDate(now);
        imageToSave.setStatus(GeorefStatus.UPLOADED);

        GeorefImage savedImage = new GeorefImage();
        savedImage.setId(imageId);
        savedImage.setHash(hash);
        savedImage.setFilepathOriginal(mockPath.toString());
        savedImage.setUploadingDate(now);
        savedImage.setStatus(GeorefStatus.UPLOADED);

        when(hashCalculator.calculate(file)).thenReturn(hash);
        when(repository.existsByHash(hash)).thenReturn(false);
        when(fileStorageService.saveOriginalFile(file, filename)).thenReturn(mockPath);
        when(georefImageFactory.create(hash, mockPath, file.getOriginalFilename())).thenReturn(imageToSave);
        when(repository.save(imageToSave)).thenReturn(savedImage);

        // WHEN
        GeorefImageDto result = georefImageService.uploadImage(file);

        // THEN
        assertNotNull(result);
        assertEquals(hash, result.getHash());
        assertEquals(imageId, result.getId());
        assertEquals(file.getOriginalFilename(), result.getFilepathOriginal());
        assertEquals(GeorefStatus.UPLOADED, result.getStatus());

        verify(hashCalculator, times(1)).calculate(file);
        verify(fileStorageService, times(1)).saveOriginalFile(file, filename);
        verify(repository, times(1)).save(imageToSave);
    }

    // Test image déjà existante : retourne l'existante sans save
    @Test
    @DisplayName("doit retourner l'image existante sans l'enregistrer à nouveau")
    void shouldReturnExistingImageDto_WhenImageAlreadyExists() throws Exception {
        // GIVEN
        byte[] content = "same image".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("file", "mock.jpg", "image/png", content);

        String hash = "existing-image-hash";
        UUID imageId = UUID.randomUUID();
        LocalDateTime uploadDate = LocalDateTime.of(2023, 10, 1, 12, 0);
        String path = "originals/" + hash + "_mock.jpg";

        GeorefImage existingImage = new GeorefImage();
        existingImage.setId(imageId);
        existingImage.setHash(hash);
        existingImage.setUploadingDate(uploadDate);
        existingImage.setFilepathOriginal(path);
        existingImage.setStatus(GeorefStatus.UPLOADED);

        when(hashCalculator.calculate(file)).thenReturn(hash);
        when(repository.existsByHash(hash)).thenReturn(true);
        when(repository.findByHash(hash)).thenReturn(existingImage);

        // WHEN
        GeorefImageDto result = georefImageService.uploadImage(file);

        // THEN
        assertNotNull(result);
        assertEquals(imageId, result.getId());
        assertEquals(hash, result.getHash());
        assertEquals(file.getOriginalFilename(), result.getFilepathOriginal());
        assertEquals(GeorefStatus.UPLOADED, result.getStatus());
        assertTrue(result.getUploadingDate().isBefore(LocalDateTime.now()));

        verify(hashCalculator, times(1)).calculate(file);
        verify(repository, never()).save(any());
        verify(fileStorageService, never()).saveOriginalFile(any(), any());
        verify(georefImageFactory, never()).create(any(), any(), any());
    }

    // Test pour un type d'image non supporté
    @ParameterizedTest
    @ValueSource(strings = {
            "text/plain",
            "application/pdf",
            "application/octet-stream",
            "application/json",
            "video/mp4",
            "audio/mpeg",
            "image/svg+xml"
    })
    @DisplayName("doit lever une exception UnsupportedImageFormatException pour les types MIME non supportés")
    void shouldThrowUnsupportedImageFormatException_ForUnsupportedMimeTypes(String mimeType) {
        // GIVEN
        byte[] content = "not an image".getBytes(StandardCharsets.UTF_8);
        MultipartFile invalidFile = new MockMultipartFile("file", "test." + mimeType.split("/")[1], mimeType, content);

        // WHEN + THEN
        assertThrows(UnsupportedImageFormatException.class, () -> georefImageService.uploadImage(invalidFile));
        verifyNoInteractions(hashCalculator, fileStorageService, georefImageFactory, repository);
    }

    @Test
    @DisplayName("doit lever ImageNotFoundException dans updateGeoreferencingParams si image non trouvée")
    void shouldThrowException_WhenUpdatingImageThatDoesNotExist() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        GeorefImageDto dto = new GeorefImageDto();
        dto.setId(imageId);

        when(repository.findById(imageId)).thenReturn(java.util.Optional.empty());

        // WHEN + THEN
        assertThrows(ImageNotFoundException.class, () -> georefImageService.updateGeoreferencingParams(dto));
        verify(repository, times(1)).findById(imageId);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteImageSuccessfully() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(image));

        // WHEN
        georefImageService.deleteImageById(id);

        // THEN
        verify(repository).delete(image);
    }

    @Test
    @DisplayName("doit lever ImageNotFoundException dans deleteImageById si image non trouvée")
    void shouldThrowException_WhenDeletingImageThatDoesNotExist() {
        // GIVEN
        UUID imageId = UUID.randomUUID();

        when(repository.findById(imageId)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(ImageNotFoundException.class, () -> georefImageService.deleteImageById(imageId));
        verify(repository).findById(imageId);
    }

    @Test
    @DisplayName("doit retourner l'image avec succès")
    void shouldGetImageSuccessfully() throws Exception {
        // GIVEN
        byte[] content = "image content".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("file", "mock.jpg", "image/png", content);

        String hash = "1a9e46aa05d390aa48745d4bda80a459e850c4b048af0c0faec37d9a2f080abb";
        String filename = hash.concat("_").concat(file.getOriginalFilename());
        UUID imageId = UUID.randomUUID();
        Path mockPath = Paths.get("georef-storage", "originals", filename);

        GeorefImage savedImage = new GeorefImage();
        savedImage.setId(imageId);
        savedImage.setHash(hash);
        savedImage.setFilepathOriginal(mockPath.toString());

        when(repository.findById(imageId)).thenReturn(Optional.of(savedImage));
        when(fileStorageService.removeHashFromFilePath(savedImage.getFilepathOriginal())).thenReturn(file.getOriginalFilename());
        when(fileStorageService.existsInOriginalDir(file.getOriginalFilename())).thenReturn(mockPath);
        
        // WHEN
        GeorefImageDto result = georefImageService.getImageById(imageId);
        
        // THEN
        assertNotNull(result);
        assertEquals(imageId, result.getId());
        assertEquals(hash, result.getHash());
        assertEquals(file.getOriginalFilename(), result.getFilepathOriginal());
    }

    @Test
    @DisplayName("doit lever IllegalArgumentException dans getImageById si l'id est nul")
    void shouldThrowIllegalArgumentException_WhenImageIdIsNull() {
        // GIVEN
        UUID id = null;

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class, () -> georefImageService.getImageById(id));
        verifyNoInteractions(repository, fileStorageService);
    }

    @Test
    @DisplayName("doit lever ImageNotFoundException dans getImageById si image non trouvée dans la base")
    void shouldThrowImageNotFoundException_WhenGettingImageThatDoesNotExist() {
        // GIVEN
        UUID imageId = UUID.randomUUID();

        when(repository.findById(imageId)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(ImageNotFoundException.class, () -> georefImageService.getImageById(imageId));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("doit lever ImageNotFoundException dans getImageById si le fichier de l'image non trouvé")
    void shouldThrowImageNotFoundException_WhenImageFileDoesNotExist() throws Exception {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        String filename = "mock.jpg";
        String hash = "1a9e46aa05d390aa48745d4bda80a459e850c4b048af0c0faec37d9a2f080abb";
        String filepath = Paths.get("georef-storage", "originals", hash.concat("_").concat(filename)).toString();

        GeorefImage image = new GeorefImage();
        image.setId(imageId);
        image.setHash(hash);
        image.setFilepathOriginal(filepath);

        when(repository.findById(imageId)).thenReturn(Optional.of(image));
        when(fileStorageService.removeHashFromFilePath(image.getFilepathOriginal())).thenReturn(filename);
        when(fileStorageService.existsInOriginalDir(filename)).thenReturn(null);

        // WHEN + THEN
        assertThrows(ImageNotFoundException.class, () -> georefImageService.getImageById(imageId));
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @DisplayName("doit retourner le fichier de l'image avec succès")
    void shouldReturnImageFileSuccessfully_whenImageExists() throws Exception {
        // GIVEN
        byte[] fileContent = "dummy image content".getBytes(StandardCharsets.UTF_8);

        File tempFile = File.createTempFile("mock", ".jpg");
        Files.write(tempFile.toPath(), fileContent);

        String hash = "1a9e46aa05d390aa48745d4bda80a459e850c4b048af0c0faec37d9a2f080abb";
        String filename = hash.concat("_").concat(tempFile.getName());
        UUID imageId = UUID.randomUUID();
        Path mockPath = Paths.get("georef-storage", "originals", filename);

        GeorefImage image = new GeorefImage();
        image.setId(imageId);
        image.setFilepathOriginal(mockPath.toString());

        when(repository.findById(imageId)).thenReturn(Optional.of(image));
        when(fileStorageService.getFileByFilePath(image.getFilepathOriginal())).thenReturn(tempFile);        

        // WHEN
        File result = georefImageService.loadOriginalImageById(imageId);

        // THEN
        assertNotNull(result);
        assertEquals(tempFile.getName(), result.getName());
        assertEquals(fileContent.length, result.length());
        assertEquals(tempFile, result);
    }

    @Test
    @DisplayName("doit lever ImageNotFoundException si l'image n'existe pas")
    void shouldThrowImageNotFoundException_whenGettingFileFromImageThatDoesNotExist() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(ImageNotFoundException.class, () -> georefImageService.loadOriginalImageById(id));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("doit lever IllegalArgumentException si l'id de l'image est nul")
    void shouldThrowIllegalArgumentException_whenImageIdIsNull() {
        // GIVEN
        UUID id = null;
        
        // WHEN + THEN
        assertThrows(IllegalArgumentException.class, () -> georefImageService.loadOriginalImageById(id));
        verifyNoInteractions(repository, fileStorageService);
    }

}