package com.amine.pfe.georef_module.image.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.pfe.georef_module.exception.CartographicServerException;
import com.amine.pfe.georef_module.exception.GeorefLayerNotFoundException;
import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.image.dto.GeorefLayerDto;
import com.amine.pfe.georef_module.image.dto.GeorefRequest;
import com.amine.pfe.georef_module.image.dto.GeorefResponse;
import com.amine.pfe.georef_module.image.dto.RegeorefResponse;
import com.amine.pfe.georef_module.image.service.GeorefLayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/georef/layer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Layers", description = "Georeferencing operation and layers management APIs")
public class GeorefLayerController {

    private final GeorefLayerService georefLayerService;

    @Operation(summary = "Géoréférencer une image", description = "Effectue le géoréférencement d'une image en fonction des paramètres fournis et publie la couche correspondante.", responses = {
            @ApiResponse(responseCode = "200", description = "Image géoréférencée avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeorefResponse.class))),
            @ApiResponse(responseCode = "404", description = "Image non trouvée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeorefResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors du géoréférencement", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeorefResponse.class)))
    })
    @PostMapping(value = "/{imageId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeorefResponse> georeferenceImage(@RequestBody GeorefRequest request,
            @PathVariable UUID imageId) {
        try {
            GeorefResponse georefResponse = georefLayerService.georeferenceImage(request, imageId);

            if (!georefResponse.isEnoughGCPs()) {
                log.warn("Échec du géoréférencement : {}", georefResponse.getMessage());
                return ResponseEntity.status(HttpStatus.OK).body(georefResponse);
            }

            log.info("Image géoréférencée et couche publiée avec succès");
            return ResponseEntity.ok(georefResponse);

        } catch (ImageNotFoundException e) {
            log.warn("Image non trouvée pour l'ID {} : {}", imageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeorefResponse(false, 0, e.getMessage()));

        } catch (Exception e) {
            log.error("Erreur inattendue lors du géoréférencement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GeorefResponse(false, 0, "Erreur interne : " + e.getMessage()));
        }
    }

    @Operation(summary = "Supprimer une couche géoréférencée", description = "Supprime une couche géoréférencée", responses = {
            @ApiResponse(responseCode = "204", description = "Couche supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Couche non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors de la suppression de la couche")
    })
    @DeleteMapping("/{layerId}")
    public ResponseEntity<Void> deleteGeorefLayerById(@PathVariable UUID layerId) {
        try {
            georefLayerService.deleteGeorefLayerById(layerId);
            log.info("Couche géoréférencée avec l'ID {} supprimée avec succès", layerId);
            return ResponseEntity.noContent().build();

        } catch (GeorefLayerNotFoundException e) {
            log.warn("Couche non trouvée pour l'ID {} : {}", layerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de la couche avec l'ID {} : {}", layerId,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Supprimer une couche géoréférencée", description = "Supprime une couche géoréférencée ainsi que son image si elle est en statut COMPLETED.", responses = {
            @ApiResponse(responseCode = "204", description = "Couche supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Couche non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors de la suppression de la couche")
    })
    @DeleteMapping("/layer-and-image/{layerId}")
    public ResponseEntity<Void> deleteGeorefLayerAndImageById(@PathVariable UUID layerId) {
        try {
            georefLayerService.deleteGeorefLayerAndImageById(layerId);
            log.info("Couche géoréférencée avec l'ID {} et son image supprimées avec succès", layerId);
            return ResponseEntity.noContent().build();

        } catch (GeorefLayerNotFoundException e) {

            log.warn("Couche non trouvée pour l'ID {} : {}", layerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (CartographicServerException e) {

            log.error("Erreur inattendue lors de la suppression de la couche avec l'ID {} : {}", layerId,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de la couche avec l'ID {} : {}", layerId,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Récupérer toutes les couches géoréférencées", responses = {
            @ApiResponse(responseCode = "200", description = "Couche supprimée avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors de la récupération des couche géoréférencées")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GeorefLayerDto>> getAllGeorefLayers() {
        try {
            List<GeorefLayerDto> georefLayerDtos = georefLayerService.getAllGeorefLayers();
            log.info("Toutes les couches géoréférencées sont récupérées avec succès");
            return ResponseEntity.status(HttpStatus.OK).body(georefLayerDtos);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des couches géoréférencées", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Préparer le re-géoréférencement", description = "Créer une copie de l'image à re-géréférencer et la retourner avec ses GCPs", responses = {
            @ApiResponse(responseCode = "200", description = "Copie crée avec succès et retournée avec ses GCPs"),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors de la préparation du re-géoréférencement")
    })
    @GetMapping(value = "/{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegeorefResponse> prepareRegeoref(@PathVariable UUID imageId) {
        try {
            
            RegeorefResponse regeorefResponse = georefLayerService.prepareRegeoref(imageId);
            log.info("Copie crée avec succès et retournée avec ses GCPs");
            return ResponseEntity.status(HttpStatus.OK).body(regeorefResponse);
        
        } catch (Exception e) {
        
            log.error("Erreur inattendue lors de la préparation du re-géoréférencement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        
        }
    }

}
