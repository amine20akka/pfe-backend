package com.amine.pfe.georef_module.image.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.entity.GeorefLayer;
import com.amine.pfe.georef_module.exception.GeorefLayerNotFoundException;
import com.amine.pfe.georef_module.image.repository.GeorefLayerRepository;
import com.amine.pfe.georef_module.image.service.port.CartographicServer;
import com.amine.pfe.georef_module.image.service.port.FileStorageService;
import com.amine.pfe.georef_module.image.service.port.GeospatialServer;

@ExtendWith(MockitoExtension.class)
class GeorefLayerServiceTest {

    @InjectMocks
    private GeorefLayerService georefLayerService;

    @Mock
    private GeorefLayerRepository georefLayerRepository;

    @Mock
    private GeorefImageService georefImageService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private GeospatialServer geospatialServer;

    @Mock
    private CartographicServer cartographicServer;

    @Test
    void deleteGeorefLayer_shouldThrowException_whenLayerNotFound() {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(georefLayerRepository.findById(unknownId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(GeorefLayerNotFoundException.class, () -> {
            georefLayerService.deleteGeorefLayerById(unknownId);
        });

        verifyNoInteractions(georefImageService, fileStorageService);
    }

    @Test
    void shouldDeleteGeorefLayerSuccessfully() throws IOException {
        // Given
        UUID layerId = UUID.randomUUID();
        GeorefImage image = GeorefImage.builder()
                .id(UUID.randomUUID())
                .outputFilename("output.tif")
                .filepathGeoreferenced("/path/to/output.tif")
                .build();

        GeorefLayer layer = GeorefLayer.builder()
                .id(layerId)
                .image(image)
                .layerName("layer1")
                .storeName("store1")
                .build();

        when(georefLayerRepository.findById(layerId)).thenReturn(Optional.of(layer));
        when(cartographicServer.deleteGeoTiffLayer("layer1", "store1")).thenReturn(true);

        // When
        georefLayerService.deleteGeorefLayerById(layerId);

        // Then
        verify(geospatialServer).deleteGeorefFile("output.tif");
        verify(fileStorageService).deleteFileByFullPath("/path/to/output.tif");
        verify(georefLayerRepository).delete(layer);
    }

}