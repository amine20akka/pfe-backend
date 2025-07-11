package com.amine.pfe.georef_module.gcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
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

@ExtendWith(MockitoExtension.class)
class GcpServiceTest {

    @Mock
    private GcpRepository gcpRepository;

    @Mock
    private GeorefImageRepository imageRepository;

    @Mock
    private GcpFactory gcpFactory;

    @Mock
    private ResidualsService residualsService;

    @InjectMocks
    private GcpService gcpService;

    @Test
    @DisplayName("should add GCP successfully")
    void shouldAddGcpSuccessfully() {
        // Given
        UUID imageId = UUID.randomUUID();

        GcpDto addGcpRequest = GcpDto.builder()
                .imageId(imageId)
                .sourceX(10.44)
                .sourceY(20.44)
                .mapX(100.0)
                .mapY(200.0)
                .index(2)
                .build();

        GeorefImage image = new GeorefImage();
        Gcp savedGcp = GcpMapper.toEntity(addGcpRequest, image);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findMaxIndexByImageId(imageId)).thenReturn(Optional.of(1));
        when(gcpRepository.existsByImageIdAndIndex(imageId, 2)).thenReturn(false);
        when(gcpFactory.createGcp(image, 10.44, 20.44, 100.0, 200.0, 2))
                .thenReturn(savedGcp);
        when(gcpRepository.save(any(Gcp.class))).thenReturn(savedGcp);

        // When
        GcpDto gcpDto = gcpService.addGcp(addGcpRequest);

        // Then
        assertNotNull(gcpDto);
        assertEquals(savedGcp.getId(), gcpDto.getId());
        verify(imageRepository, times(1)).findById(imageId);
        verify(gcpRepository, times(1)).save(any(Gcp.class));
    }

    @Test
    @DisplayName("should throw when imageId is null")
    void shouldThrowWhenImageIdIsNull() {
        // Given
        GcpDto addGcpRequest = GcpDto.builder().imageId(null).build();

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> gcpService.addGcp(addGcpRequest));
        assertThrows(IllegalArgumentException.class, () -> gcpService.getGcpsByImageId(null));
        verifyNoInteractions(imageRepository);
        verifyNoInteractions(gcpRepository);
    }

    @Test
    @DisplayName("should throw when image not found")
    void shouldThrowWhenImageNotFound() {
        // Given
        UUID imageId = UUID.randomUUID();
        GcpDto addGcpRequest = GcpDto.builder().imageId(imageId).build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(ImageNotFoundException.class, () -> gcpService.addGcp(addGcpRequest));
        verify(imageRepository, times(1)).findById(imageId);
        verifyNoInteractions(gcpRepository);
    }

    @Test
    @DisplayName("should throw when duplicate index by image")
    void shouldThrowWhenDuplicateIndexByImage() {
        // Given
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        GcpDto addGcpRequest = GcpDto.builder().imageId(imageId).index(1).build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.existsByImageIdAndIndex(imageId, 1)).thenReturn(true);

        // When + Then
        assertThrows(DuplicateGcpIndexException.class, () -> gcpService.addGcp(addGcpRequest));
        verify(gcpRepository, never()).save(any(Gcp.class));
    }

    @Test
    @DisplayName("should get GCPs by image ID successfully")
    void shouldGetGcpsByImageIdSuccessfully() {
        // Given
        GeorefImage image = new GeorefImage();
        image.setId(UUID.randomUUID());

        Gcp gcp = new Gcp();
        gcp.setId(UUID.randomUUID());
        gcp.setImage(image);

        when(imageRepository.findById(image.getId())).thenReturn(Optional.of(image));
        when(gcpRepository.findByImageId(image.getId())).thenReturn(List.of(gcp));

        // When
        List<GcpDto> gcps = gcpService.getGcpsByImageId(image.getId());

        // Then
        assertNotNull(gcps);
        assertEquals(1, gcps.size());
        verify(gcpRepository, times(1)).findByImageId(image.getId());
    }

    @Test
    @DisplayName("doit supprimer le dernier GCP sans réindexer les GCPs restants")
    void shouldDeleteGcpWithoutReindexing_WhenDeletingLastGcp() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        UUID gcpIdToDelete = UUID.randomUUID();

        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        Gcp gcpToDelete = new Gcp();
        gcpToDelete.setId(gcpIdToDelete);
        gcpToDelete.setImage(image);
        gcpToDelete.setIndex(3);

        Gcp gcp1 = new Gcp();
        gcp1.setId(UUID.randomUUID());
        gcp1.setImage(image);
        gcp1.setIndex(1);
        Gcp gcp2 = new Gcp();
        gcp2.setId(UUID.randomUUID());
        gcp2.setImage(image);
        gcp2.setIndex(2);

        image.setGcps(List.of(gcp1, gcp2, gcpToDelete));

        when(gcpRepository.findById(gcpIdToDelete)).thenReturn(Optional.of(gcpToDelete));
        when(gcpRepository.findAllByImageIdOrderByIndex(imageId)).thenReturn(List.of(gcp1, gcp2));

        // WHEN
        gcpService.deleteGcpById(gcpIdToDelete);

        // THEN
        verify(gcpRepository, times(1)).delete(gcpToDelete);
        verify(gcpRepository, never()).saveAll(anyList());

        assertEquals(1, gcp1.getIndex());
        assertEquals(2, gcp2.getIndex());
    }

    @Test
    @DisplayName("doit supprimer le GCP et réindexer les GCPs restants")
    void shouldDeleteGcpAndReindexing_WhenDeletingGcpIsNotLast() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        UUID gcpIdToDelete = UUID.randomUUID();

        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        Gcp gcpToDelete = new Gcp();
        gcpToDelete.setId(gcpIdToDelete);
        gcpToDelete.setImage(image);
        gcpToDelete.setIndex(2);

        Gcp gcp1 = new Gcp();
        gcp1.setId(UUID.randomUUID());
        gcp1.setImage(image);
        gcp1.setIndex(1);
        Gcp gcp3 = new Gcp();
        gcp3.setId(UUID.randomUUID());
        gcp3.setImage(image);
        gcp3.setIndex(3);

        image.setGcps(List.of(gcp1, gcp3, gcpToDelete));

        when(gcpRepository.findById(gcpIdToDelete)).thenReturn(Optional.of(gcpToDelete));
        when(gcpRepository.findAllByImageIdOrderByIndex(imageId)).thenReturn(List.of(gcp1, gcp3));

        // WHEN
        gcpService.deleteGcpById(gcpIdToDelete);

        // THEN
        verify(gcpRepository, times(1)).delete(gcpToDelete);
        verify(gcpRepository, times(1)).saveAll(anyList());

        assertEquals(1, gcp1.getIndex());
        assertEquals(2, gcp3.getIndex());
    }

    @Test
    @DisplayName("doit lever GcpNotFoundException si GCP non trouvé")
    void shouldThrowExceptionWhenGcpNotFound() {
        // GIVEN
        UUID id = UUID.randomUUID();
        when(gcpRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(GcpNotFoundException.class, () -> gcpService.deleteGcpById(id));
        verify(gcpRepository, times(1)).findById(id);
        verify(gcpRepository, never()).delete(any(Gcp.class));
        verify(gcpRepository, never()).findAllByImageIdOrderByIndex(any(UUID.class));
        verify(gcpRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("should update GCP successfully")
    void updateGcpSuccessfully() {
        // GIVEN
        UUID gcpId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        Gcp existingGcp = new Gcp();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);
        existingGcp.setId(gcpId);
        existingGcp.setImage(image);
        existingGcp.setIndex(2);
        existingGcp.setSourceX(50.44);
        existingGcp.setSourceY(100.44);
        existingGcp.setMapX(5.0);
        existingGcp.setMapY(10.0);

        GcpDto incomingDto = new GcpDto();
        incomingDto.setId(gcpId);
        incomingDto.setImageId(imageId);
        incomingDto.setSourceX(100.44);
        incomingDto.setSourceY(200.44);
        incomingDto.setMapX(10.0);
        incomingDto.setMapY(20.0);
        incomingDto.setIndex(2);

        when(gcpRepository.findById(gcpId)).thenReturn(Optional.of(existingGcp));

        // WHEN
        GcpDto result = gcpService.updateGcp(incomingDto);

        // THEN
        assertEquals(result.getId(), gcpId);
        assertEquals(result.getSourceX(), incomingDto.getSourceX());
        assertEquals(result.getSourceY(), incomingDto.getSourceY());
        assertEquals(result.getMapX(), incomingDto.getMapX());
        assertEquals(result.getMapY(), incomingDto.getMapY());
        assertEquals(result.getIndex(), incomingDto.getIndex());
        assertEquals(result.getImageId(), imageId);
        verify(gcpRepository).findById(gcpId);
    }

    @Test
    @DisplayName("should throw GcpNotFoundException when GCP not found")
    void shouldThrowGcpNotFoundException_WhenUpdatingGcp() {
        // GIVEN
        UUID gcpId = UUID.randomUUID();
        GcpDto incomingDto = new GcpDto();
        incomingDto.setId(gcpId);

        when(gcpRepository.findById(gcpId)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(GcpNotFoundException.class, () -> gcpService.updateGcp(incomingDto));
        assertNotNull(incomingDto.getId());
        verify(gcpRepository, never()).save(any(Gcp.class));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when GCP ID is null")
    void shouldThrowIllegalArgumentException_WhenUpdatingGcp() {
        // GIVEN
        UUID gcpId = null;
        GcpDto incomingDto = new GcpDto();
        incomingDto.setId(gcpId);

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class, () -> gcpService.updateGcp(incomingDto));
        verifyNoInteractions(gcpRepository);
    }

    @Test
    @DisplayName("should return calculated residuals successfully")
    void shouldComputeAndUpdateResidualsSuccessfully() {
        // GIVEN
        UUID gcp1Id = UUID.randomUUID();
        UUID gcp2Id = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        TransformationType type = TransformationType.POLYNOMIALE_1;
        List<Double> residuals = List.of(0.5, 1.2);

        Gcp gcp1 = new Gcp();
        gcp1.setId(gcp1Id);
        gcp1.setImage(image);
        gcp1.setResidual(null);
        Gcp gcp2 = new Gcp();
        gcp2.setId(gcp2Id);
        gcp2.setImage(image);
        gcp2.setResidual(null);

        List<Gcp> gcps = List.of(gcp1, gcp2);

        Gcp updatedGcp1 = new Gcp();
        updatedGcp1.setId(gcp1Id);
        updatedGcp1.setImage(image);
        updatedGcp1.setResidual(0.5);
        Gcp updatedGcp2 = new Gcp();
        updatedGcp2.setId(gcp2Id);
        updatedGcp2.setImage(image);
        updatedGcp2.setResidual(1.2);

        List<Gcp> updatedGcps = List.of(updatedGcp1, updatedGcp2);
        List<GcpDto> updatedGcpDtos = List.of(GcpMapper.toDto(updatedGcp1), GcpMapper.toDto(updatedGcp2));

        ResidualsRequest request = new ResidualsRequest();
        request.setImageId(imageId);
        request.setType(type);
        request.setSrid(Srid._3857);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findByImageId(imageId)).thenReturn(gcps);
        when(residualsService.getMinimumPointsRequired(type)).thenReturn(3);
        when(residualsService.hasEnoughGCPs(anyList(), eq(type))).thenReturn(true);
        when(residualsService.computeResiduals(anyList(), eq(type), eq(Srid._3857)))
                .thenReturn(new ResidualsResult(residuals, 1.0));
        when(gcpRepository.saveAll(gcps)).thenReturn(updatedGcps);

        // WHEN
        ResidualsResponse response = gcpService.updateResiduals(request);

        // THEN
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1.0, response.getRmse());
        assertEquals(2, response.getGcpDtos().size());
        assertEquals(updatedGcpDtos.get(0).getId(), response.getGcpDtos().get(0).getId());
        assertEquals(updatedGcpDtos.get(0).getResidual(), response.getGcpDtos().get(0).getResidual());
        assertEquals(updatedGcpDtos.get(1).getId(), response.getGcpDtos().get(1).getId());
        assertEquals(updatedGcpDtos.get(1).getResidual(), response.getGcpDtos().get(1).getResidual());
        verify(gcpRepository).saveAll(gcps);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when imageId is null")
    void shouldThrowIllegalArgumentException_WhenImageIdIsNull() {
        ResidualsRequest request = new ResidualsRequest();
        request.setImageId(null);

        assertThrows(IllegalArgumentException.class, () -> gcpService.updateResiduals(request));
    }

    @Test
    @DisplayName("should throw GcpNotFoundException when no GCPs found")
    void shouldThrowGcpNotFoundException_WhenNoGcpFound() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        ResidualsRequest request = new ResidualsRequest();
        request.setImageId(imageId);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findByImageId(imageId)).thenReturn(Collections.emptyList());

        // WHEN + THEN
        assertThrows(GcpNotFoundException.class, () -> gcpService.updateResiduals(request));
        verify(imageRepository).findById(imageId);
        verify(gcpRepository).findByImageId(imageId);
        verifyNoInteractions(residualsService);
        verify(gcpRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("should clear residuals when not enough GCPs")
    void shouldClearResidualsWhenNotEnoughGcps() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        Gcp gcp1 = new Gcp();
        gcp1.setImage(image);
        gcp1.setResidual(null);
        Gcp gcp2 = new Gcp();
        gcp2.setImage(image);
        gcp2.setResidual(null);

        List<Gcp> gcps = List.of(gcp1, gcp2);
        TransformationType type = TransformationType.POLYNOMIALE_1;

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findByImageId(imageId)).thenReturn(gcps);
        when(residualsService.getMinimumPointsRequired(type)).thenReturn(3);
        when(residualsService.hasEnoughGCPs(anyList(), eq(type))).thenReturn(false);
        when(gcpRepository.saveAll(anyList())).thenReturn(gcps);

        ResidualsRequest request = new ResidualsRequest();
        request.setImageId(imageId);
        request.setType(type);

        // WHEN
        ResidualsResponse response = gcpService.updateResiduals(request);

        // THEN
        assertFalse(response.isSuccess());
        assertNull(response.getRmse());
        assertEquals(response.getMinPointsRequired(), 3);
        assertEquals(response.getGcpDtos().get(0).getResidual(), null);
        assertEquals(response.getGcpDtos().get(1).getResidual(), null);
        verify(gcpRepository, times(1)).findByImageId(imageId);
        verify(gcpRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("should load and return GcpDtos successfully when overwrite is true")
    void shouldLoadGcpsSuccessfully_WithOverwriteTrue() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        GcpDto dto1 = new GcpDto(10.44, 20.44, 30.44, 40.44);
        GcpDto dto2 = new GcpDto(50.44, 60.44, 70.44, 80.44);
        LoadGcpsRequest request = new LoadGcpsRequest(imageId, List.of(dto1, dto2), true);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findMaxIndexByImageId(imageId)).thenReturn(Optional.empty());

        Gcp gcp1 = new Gcp();
        gcp1.setId(UUID.randomUUID());
        gcp1.setImage(image);
        gcp1.setIndex(1);

        Gcp gcp2 = new Gcp();
        gcp2.setId(UUID.randomUUID());
        gcp2.setImage(image);
        gcp2.setIndex(2);

        when(gcpFactory.createGcp(eq(image), eq(10.44), eq(20.44), eq(30.44), eq(40.44), eq(1))).thenReturn(gcp1);
        when(gcpFactory.createGcp(eq(image), eq(50.44), eq(60.44), eq(70.44), eq(80.44), eq(2))).thenReturn(gcp2);
        when(gcpRepository.saveAll(anyList())).thenReturn(List.of(gcp1, gcp2));

        // WHEN
        List<GcpDto> result = gcpService.loadGcps(request);

        // THEN
        verify(gcpRepository).deleteByImageId(imageId);
        assertEquals(1, result.get(0).getIndex());
        assertEquals(2, result.get(1).getIndex());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should load and return GcpDtos successfully when overwrite is false")
    void shouldLoadGcpsSuccessfully_WithOverwriteFalse() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        GcpDto dto1 = new GcpDto(10.44, 20.44, 30.44, 40.44);
        GcpDto dto2 = new GcpDto(50.44, 60.44, 70.44, 80.44);
        LoadGcpsRequest request = new LoadGcpsRequest(imageId, List.of(dto1, dto2), false);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findMaxIndexByImageId(imageId)).thenReturn(Optional.of(2));

        Gcp gcp1 = new Gcp();
        gcp1.setId(UUID.randomUUID());
        gcp1.setImage(image);
        gcp1.setIndex(3);

        Gcp gcp2 = new Gcp();
        gcp2.setId(UUID.randomUUID());
        gcp2.setImage(image);
        gcp2.setIndex(4);

        when(gcpFactory.createGcp(eq(image), eq(10.44), eq(20.44), eq(30.44), eq(40.44), eq(3))).thenReturn(gcp1);
        when(gcpFactory.createGcp(eq(image), eq(50.44), eq(60.44), eq(70.44), eq(80.44), eq(4))).thenReturn(gcp2);
        when(gcpRepository.saveAll(anyList())).thenReturn(List.of(gcp1, gcp2));

        // WHEN
        List<GcpDto> result = gcpService.loadGcps(request);

        // THEN
        verify(gcpRepository, never()).deleteByImageId(imageId);
        assertEquals(3, result.get(0).getIndex());
        assertEquals(4, result.get(1).getIndex());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when imageId is null when trying to load GCPs")
    void shouldThrowIllegalArgumentException_WhenImageIdIsNull_WhileLoadingGcps() {
        // GIVEN
        LoadGcpsRequest request = new LoadGcpsRequest(null, List.of(new GcpDto(1.0, 1.0, 2.0, 2.0)), false);

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class, () -> gcpService.loadGcps(request));
    }

    @Test
    @DisplayName("should throw ImageNotFoundException when image not found when trying to load GCPs")
    void shouldThrowImageNotFoundException_WhenImageNotFound_WhileLoadingGcps() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        LoadGcpsRequest request = new LoadGcpsRequest(imageId, List.of(new GcpDto(1.0, 1.0, 2.0, 2.0)), false);

        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(ImageNotFoundException.class, () -> gcpService.loadGcps(request));
    }

    @Test
    @DisplayName("should throw DuplicateGcpIndexException when index already exists when trying to load GCPs")
    void shouldThrowDuplicateGcpIndexException_WhenIndexAlreadyExists_WhileLoadingGcps() {
        // GIVEN
        UUID imageId = UUID.randomUUID();
        GeorefImage image = new GeorefImage();
        image.setId(imageId);

        LoadGcpsRequest request = new LoadGcpsRequest(imageId, List.of(new GcpDto(1.0, 1.0, 2.0, 2.0)), false);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(gcpRepository.findMaxIndexByImageId(imageId)).thenReturn(Optional.of(0));
        when(gcpRepository.existsByImageIdAndIndex(imageId, 1)).thenReturn(true);

        // WHEN + THEN
        assertThrows(DuplicateGcpIndexException.class, () -> gcpService.loadGcps(request));
    }

    @Test
    @DisplayName("should return true and delete successfully all GCPs of an image")
    void deleteAllByImageId_shouldReturnTrue_whenGcpListIsNotEmpty() {
        // Given
        UUID imageId = UUID.randomUUID();
        List<Gcp> gcpList = List.of(
                new Gcp(), new Gcp()
        );

        when(gcpRepository.findAllByImageId(imageId)).thenReturn(gcpList);

        // When
        boolean result = gcpService.deleteAllGcpsByImageId(imageId);

        // Then
        assertTrue(result);
        verify(gcpRepository).deleteAll(gcpList);
    }

    @Test
    @DisplayName("should return false when the image has no GCPs to delete")
    void deleteAllByImageId_shouldReturnFalse_whenGcpListIsEmpty() {
        // Given
        UUID imageId = UUID.randomUUID();
        when(gcpRepository.findAllByImageId(imageId)).thenReturn(Collections.emptyList());

        // When
        boolean result = gcpService.deleteAllGcpsByImageId(imageId);

        // Then
        assertFalse(result);
        verify(gcpRepository, never()).deleteAll(anyList());
    }
}