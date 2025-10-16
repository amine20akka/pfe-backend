package com.amine.pfe.georef_module.gcp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.amine.pfe.georef_module.exception.ImageNotFoundException;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.LoadGcpsRequest;
import com.amine.pfe.georef_module.gcp.dto.ResidualsRequest;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResponse;
import com.amine.pfe.georef_module.gcp.exceptions.DuplicateGcpIndexException;
import com.amine.pfe.georef_module.gcp.exceptions.GcpNotFoundException;
import com.amine.pfe.georef_module.gcp.service.GcpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST controller for Ground Control Point (GCP) management in the
 * georeferencing module.
 * 
 * <p>
 * This controller provides CRUD operations and analysis endpoints for managing
 * GCPs,
 * which are essential reference points for image georeferencing. It handles GCP
 * creation,
 * retrieval, updates, deletion, and residual computation.
 * </p>
 * 
 * <p>
 * <b>Base Path:</b> {@code /georef/gcp}
 * </p>
 * 
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 * <li>Individual GCP operations (add, update, delete, retrieve)</li>
 * <li>Bulk GCP loading from external sources (JSON)</li>
 * <li>Residual analysis and RMSE computation</li>
 * <li>Comprehensive error handling with appropriate HTTP status codes</li>
 * </ul>
 * 
 * <p>
 * <b>Error Handling Strategy:</b>
 * </p>
 * <p>
 * All endpoints implement try-catch blocks to handle specific exceptions and
 * return
 * appropriate HTTP status codes. Errors are logged via SLF4J for monitoring and
 * debugging.
 * </p>
 * 
 * <p>
 * <b>Documentation:</b>
 * </p>
 * <p>
 * For detailed API documentation including request/response schemas and
 * examples,
 * refer to the Swagger UI at: {@code /swagger-ui.html}
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @see GcpService
 * @see GcpDto
 */
@RestController
@RequestMapping("/georef/gcp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GCP", description = "API pour la gestion des Ground Control Points")
public class GcpController {

    private final GcpService gcpService;

    @Operation(summary = "Ajouter un GCP", description = "Permet d'ajouter un GCP à une image géoréférencée.", responses = {
            @ApiResponse(responseCode = "200", description = "GCP ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation des données d'entrée"),
            @ApiResponse(responseCode = "404", description = "Image introuvable"),
            @ApiResponse(responseCode = "409", description = "Erreur de doublon d'index GCP"),
            @ApiResponse(responseCode = "500", description = "Erreur inattendue lors de l'ajout d'un GCP")
    })
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GcpDto> addGcp(@RequestBody GcpDto gcpDto) {
        try {

            GcpDto gcpDtoResponse = gcpService.addGcp(gcpDto);
            log.info("GCP ajouté avec succès : {}", gcpDtoResponse);
            return ResponseEntity.status(200).body(gcpDtoResponse);

        } catch (IllegalArgumentException e) {

            log.error("Erreur de validation des données d'entrée : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);

        } catch (ImageNotFoundException e) {

            log.error("Image introuvable : {}", e.getMessage(), e);
            return ResponseEntity.status(404).body(null);

        } catch (DuplicateGcpIndexException e) {

            log.error("Erreur de doublon d'index GCP : {}", e.getMessage(), e);
            return ResponseEntity.status(409).body(null);

        } catch (Exception e) {

            log.error("Erreur inattendue lors de l'ajout d'un GCP : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);

        }
    }

    @Operation(summary = "Récupérer les GCPs par ID d'image", description = "Permet de récupérer tous les GCPs associés à une image géoréférencée.", responses = {
            @ApiResponse(responseCode = "200", description = "Liste des GCPs récupérée avec succès"),
            @ApiResponse(responseCode = "400", description = "Erreur de validation des données d'entrée"),
            @ApiResponse(responseCode = "404", description = "Image introuvable"),
            @ApiResponse(responseCode = "500", description = "Erreur inattendue lors de la récupération des GCPs")
    })
    @GetMapping(value = "/{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GcpDto>> getGcpsByImageId(@PathVariable UUID imageId) {
        try {

            List<GcpDto> gcps = gcpService.getGcpsByImageId(imageId);
            log.info("Liste des GCPs récupérée avec succès pour l'image ID {} : {}", imageId, gcps);
            return ResponseEntity.status(200).body(gcps);

        } catch (IllegalArgumentException e) {

            log.error("Erreur de validation des données d'entrée : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);

        } catch (ImageNotFoundException e) {

            log.error("Image introuvable : {}", e.getMessage(), e);
            return ResponseEntity.status(404).body(null);

        } catch (Exception e) {

            log.error("Erreur inattendue lors de la récupération des GCPs : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);

        }
    }

    @Operation(summary = "Supprimer un GCP par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "GCP supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "GCP non trouvé", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GcpDto>> deleteGcpById(@PathVariable UUID id) {
        try {

            List<GcpDto> updatedGcpDtos = gcpService.deleteGcpById(id);
            log.info("GCP supprimé avec succès : {}", id);
            return ResponseEntity.status(200).body(updatedGcpDtos);

        } catch (GcpNotFoundException e) {

            log.error("Erreur lors de la suppression du GCP : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {

            log.error("Erreur inattendue lors de la suppression du GCP : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }

    @Operation(summary = "Update GCP", description = "Update an already existing GCP", responses = {
            @ApiResponse(responseCode = "200", description = "GCP updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "GCP not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error during GCP update")
    })
    @PutMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GcpDto> updateGcp(@RequestBody GcpDto gcpDto) {
        try {

            GcpDto updatedGcp = gcpService.updateGcp(gcpDto);
            log.info("GCP updated successfully: {}", updatedGcp.getId());
            return ResponseEntity.status(200).body(updatedGcp);

        } catch (IllegalArgumentException e) {

            log.error("Invalid input data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);

        } catch (GcpNotFoundException e) {

            log.error("GCP not found: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body(null);

        } catch (Exception e) {

            log.error("Unexpected error during GCP update: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);

        }
    }

    @Operation(summary = "Update Residuals", description = "Update the residuals of a list of GCPs", responses = {
            @ApiResponse(responseCode = "200", description = "Residuals updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error during residuals update")
    })
    @PutMapping(value = "/residuals", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResidualsResponse> updateResiduals(@RequestBody ResidualsRequest residualsRequest) {
        try {

            ResidualsResponse residualsResponse = gcpService.updateResiduals(residualsRequest);
            log.info("Residuals updated successfully for image ID {}: {}", residualsRequest.getImageId(),
                    residualsResponse);
            return ResponseEntity.ok(residualsResponse);

        } catch (IllegalArgumentException e) {

            log.error("Invalid input data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);

        } catch (GcpNotFoundException e) {

            log.error("GCPs not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (Exception e) {

            log.error("Unexpected error during residuals update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

        }
    }

    @Operation(summary = "Load GCPs", description = "Load a list of GCPs via a JSON file.", responses = {
            @ApiResponse(responseCode = "200", description = "GCPs added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Not Found Image"),
            @ApiResponse(responseCode = "409", description = "Duplicated GCP Index"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while loading GCPs")
    })
    @PostMapping(value = "/load", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GcpDto>> loadGcps(@RequestBody LoadGcpsRequest request) {
        try {

            List<GcpDto> gcpDtosResponse = gcpService.loadGcps(request);
            log.info("GCPs added successfully : {}", gcpDtosResponse);
            return ResponseEntity.status(200).body(gcpDtosResponse);

        } catch (IllegalArgumentException e) {

            log.error("Invalid input data : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);

        } catch (ImageNotFoundException e) {

            log.error("Not Found Image : {}", e.getMessage(), e);
            return ResponseEntity.status(404).body(null);

        } catch (DuplicateGcpIndexException e) {

            log.error("Duplicated GCP Index : {}", e.getMessage(), e);
            return ResponseEntity.status(409).body(null);

        } catch (Exception e) {

            log.error("Unexpected error while loading GCPs : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);

        }
    }

    @Operation(summary = "Supprimer tous les GCPs", description = "Supprimer tous les GCPs par ID d'image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tous les GCPs supprimés avec succès"),
            @ApiResponse(responseCode = "404", description = "Aucun GCP trouvé pour cette image", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @DeleteMapping(value = "/all/{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllGcpsByImageId(@PathVariable UUID imageId) {
        try {
            boolean deleted = gcpService.deleteAllGcpsByImageId(imageId);

            if (!deleted) {
                log.warn("Aucun GCP trouvé à supprimer pour l'image {}", imageId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            log.info("Tous les GCPs de l'image {} ont été supprimés avec succès", imageId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
        
            log.error("Erreur lors de la suppression des GCPs pour l'image {} : {}", imageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        
        }
    }

}