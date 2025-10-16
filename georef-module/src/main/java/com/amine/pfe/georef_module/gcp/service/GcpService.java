package com.amine.pfe.georef_module.gcp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
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

import lombok.RequiredArgsConstructor;

/**
 * Service layer for Ground Control Point (GCP) management and operations.
 * 
 * <p>
 * This service orchestrates all GCP-related business logic in the
 * georeferencing module,
 * including CRUD operations, bulk loading, residual computation, and index
 * management. It acts
 * as the central coordinator between the controller layer, domain logic, and
 * data access layer.
 * </p>
 * 
 * <p>
 * <b>Core Responsibilities:</b>
 * </p>
 * <ul>
 * <li>GCP lifecycle management (create, read, update, delete)</li>
 * <li>Automatic index assignment and re-indexing after deletions</li>
 * <li>Bulk GCP loading with overwrite support</li>
 * <li>Residual analysis and RMSE computation</li>
 * <li>Validation and error handling for business rules</li>
 * <li>Transaction management for data consistency</li>
 * </ul>
 * 
 * <p>
 * <b>Business Rules Enforced:</b>
 * </p>
 * <ul>
 * <li>Each GCP must belong to a valid georeferenced image</li>
 * <li>GCP indices must be unique within an image and sequential (1, 2, 3,
 * ...)</li>
 * <li>Indices are automatically reassigned after GCP deletion to maintain
 * sequence</li>
 * <li>Residual computation requires minimum GCPs based on transformation
 * type</li>
 * <li>Residuals are cleared when insufficient GCPs are available</li>
 * </ul>
 * 
 * <p>
 * <b>Architecture Pattern:</b>
 * </p>
 * <p>
 * Follows the Service Layer pattern with clear separation of concerns:
 * </p>
 * <ul>
 * <li>Uses {@link GcpRepository} for data access</li>
 * <li>Uses {@link GcpFactory} for entity creation (Factory pattern)</li>
 * <li>Uses {@link ResidualsService} for computational algorithms</li>
 * <li>Uses {@link GcpMapper} for DTO/entity transformation</li>
 * </ul>
 * 
 * <p>
 * <b>Transaction Management:</b>
 * </p>
 * <p>
 * Critical operations are annotated with {@code @Transactional} to ensure ACID
 * properties.
 * Operations like delete with re-indexing, residual updates, and bulk loading
 * must complete
 * atomically or roll back entirely on failure.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @see Gcp
 * @see GcpDto
 * @see GcpRepository
 * @see ResidualsService
 */
@Service
@RequiredArgsConstructor
public class GcpService {

        private final GcpRepository gcpRepository;
        private final GeorefImageRepository imageRepository;
        private final GcpFactory gcpFactory;
        private final ResidualsService residualsService;

        /**
         * Adds a new Ground Control Point to a georeferenced image.
         * 
         * <p>
         * This method automatically assigns the next sequential index to the new GCP by
         * finding
         * the maximum existing index and incrementing it. If no GCPs exist for the
         * image, index 1
         * is assigned.
         * </p>
         * 
         * <p>
         * <b>Business Logic:</b>
         * </p>
         * <ol>
         * <li>Validates that the image exists</li>
         * <li>Determines the next available index</li>
         * <li>Validates that the index doesn't already exist (race condition
         * check)</li>
         * <li>Creates and persists the GCP entity</li>
         * </ol>
         * 
         * <p>
         * <b>Index Assignment:</b> Automatically managed; clients should not specify an
         * index
         * in the input DTO.
         * </p>
         * 
         * @param gcpDto the GCP data including source/map coordinates and parent image
         *               ID
         * @return the created GCP with assigned ID and index
         * @throws IllegalArgumentException   if imageId is null
         * @throws ImageNotFoundException     if the image doesn't exist
         * @throws DuplicateGcpIndexException if the computed index already exists (rare
         *                                    race condition)
         */
        public GcpDto addGcp(GcpDto gcpDto) {

                UUID imageId = gcpDto.getImageId();
                validateImageIdNotNull(imageId);

                GeorefImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> {
                                        return new ImageNotFoundException(
                                                        "Image avec l'ID " + imageId + " introuvable.");
                                });

                Integer nextIndex = gcpRepository.findMaxIndexByImageId(imageId)
                                .map(maxIndex -> maxIndex + 1)
                                .orElse(1);

                if (gcpRepository.existsByImageIdAndIndex(imageId, nextIndex)) {
                        throw new DuplicateGcpIndexException("Un GCP avec ce même index existe déjà pour cette image.");
                }

                Gcp gcp = gcpFactory.createGcp(image,
                                gcpDto.getSourceX(),
                                gcpDto.getSourceY(),
                                gcpDto.getMapX(),
                                gcpDto.getMapY(),
                                nextIndex);

                Gcp saved = gcpRepository.save(gcp);
                return GcpMapper.toDto(saved);
        }

        /**
         * Retrieves all Ground Control Points associated with a specific georeferenced
         * image.
         * 
         * <p>
         * Returns an empty list if no GCPs exist for the image. The order of returned
         * GCPs
         * is determined by the repository query (typically by index or creation order).
         * </p>
         * 
         * @param imageId the unique identifier of the georeferenced image
         * @return list of GCPs for the image (empty if none exist)
         * @throws IllegalArgumentException if imageId is null
         * @throws ImageNotFoundException   if the image doesn't exist
         */
        public List<GcpDto> getGcpsByImageId(UUID imageId) {

                validateImageIdNotNull(imageId);

                GeorefImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> {
                                        return new ImageNotFoundException(
                                                        "Image avec l'ID " + imageId + " introuvable.");
                                });

                List<Gcp> gcps = gcpRepository.findByImageId(image.getId());
                return GcpMapper.toGcpDtoList(gcps);
        }

        /**
         * Deletes a GCP by ID and automatically re-indexes remaining GCPs.
         * 
         * <p>
         * <b>Re-indexing Logic:</b>
         * </p>
         * <p>
         * After deletion, if the deleted GCP's index was not the last one, all
         * subsequent
         * GCPs are re-indexed to maintain a sequential, gap-free sequence (1, 2, 3,
         * ...).
         * </p>
         * 
         * <p>
         * <b>Example:</b>
         * </p>
         * 
         * <pre>
         * Before deletion: GCPs with indices [1, 2, 3, 4, 5]
         * Delete GCP with index 3
         * After deletion: GCPs re-indexed to [1, 2, 3, 4]
         * </pre>
         * 
         * <p>
         * <b>Transaction Boundary:</b> This operation is transactional to ensure
         * atomicity.
         * If re-indexing fails, the deletion is rolled back.
         * </p>
         * 
         * @param gcpId the unique identifier of the GCP to delete
         * @return list of remaining GCPs after deletion and re-indexing
         * @throws GcpNotFoundException if no GCP exists with the given ID
         */
        @Transactional
        public List<GcpDto> deleteGcpById(UUID gcpId) {
                Gcp gcpToDelete = gcpRepository.findById(gcpId)
                                .orElseThrow(() -> new GcpNotFoundException("GCP non trouvé avec l'id : " + gcpId));

                UUID imageId = gcpToDelete.getImage().getId();
                int indexToDelete = gcpToDelete.getIndex();

                gcpRepository.delete(gcpToDelete);

                // Retrieve remaining GCPs in order
                List<Gcp> remainingGcps = gcpRepository.findAllByImageIdOrderByIndex(imageId);

                // Re-index if necessary (if deleted GCP was not the last one)
                if (indexToDelete < remainingGcps.size() + 1) {
                        for (int i = 0; i < remainingGcps.size(); i++) {
                                remainingGcps.get(i).setIndex(i + 1);
                        }
                        remainingGcps = gcpRepository.saveAll(remainingGcps);
                }

                return GcpMapper.toGcpDtoList(remainingGcps);
        }

        /**
         * Updates an existing Ground Control Point's coordinate data.
         * 
         * <p>
         * Only the coordinate fields (sourceX, sourceY, mapX, mapY) are updated. The
         * index,
         * image association, and residual values are not modified by this operation.
         * </p>
         * 
         * <p>
         * <b>Note:</b> After updating GCP coordinates, residuals should be recalculated
         * via
         * {@link #updateResiduals(ResidualsRequest)} to reflect the changes.
         * </p>
         * 
         * <p>
         * <b>Transaction Boundary:</b> Runs within a transaction for consistency.
         * </p>
         * 
         * @param gcpDto the GCP data with updated coordinates; must include the GCP ID
         * @return the updated GCP
         * @throws IllegalArgumentException if the GCP ID is null
         * @throws GcpNotFoundException     if no GCP exists with the given ID
         */
        @Transactional
        public GcpDto updateGcp(GcpDto gcpDto) {
                if (gcpDto.getId() == null) {
                        throw new IllegalArgumentException("GCP ID cannot be null.");
                }

                Gcp gcpToUpdate = gcpRepository.findById(gcpDto.getId())
                                .orElseThrow(() -> new GcpNotFoundException(
                                                "GCP not found : " + gcpDto.getId()));

                // Update only coordinate fields
                gcpToUpdate.setSourceX(gcpDto.getSourceX());
                gcpToUpdate.setSourceY(gcpDto.getSourceY());
                gcpToUpdate.setMapX(gcpDto.getMapX());
                gcpToUpdate.setMapY(gcpDto.getMapY());

                return GcpMapper.toDto(gcpToUpdate);
        }

        /**
         * Computes transformation residuals and RMSE for all GCPs of an image.
         * 
         * <p>
         * This method performs the complete residual analysis workflow:
         * </p>
         * <ol>
         * <li>Validates that the image exists and has GCPs</li>
         * <li>Checks if sufficient GCPs exist for the requested transformation
         * type</li>
         * <li>If insufficient: clears residuals and returns failure response</li>
         * <li>If sufficient: computes residuals using the {@link ResidualsService}</li>
         * <li>Updates each GCP entity with its computed residual value</li>
         * <li>Updates the image's mean residual (RMSE) for quick reference</li>
         * </ol>
         * 
         * <p>
         * <b>Minimum GCPs Required:</b>
         * </p>
         * <ul>
         * <li>POLYNOMIALE_1 (Affine): 3 GCPs</li>
         * <li>POLYNOMIALE_2: 6 GCPs</li>
         * <li>POLYNOMIALE_3: 10 GCPs</li>
         * </ul>
         * 
         * <p>
         * <b>Residual Rounding:</b> Residuals are rounded to 4 decimal places for
         * storage
         * and display consistency.
         * </p>
         * 
         * <p>
         * <b>Transaction Boundary:</b> Ensures atomic updates of all GCPs and the
         * parent image.
         * </p>
         * 
         * @param residualsRequest the request specifying image ID, transformation type,
         *                         and SRID
         * @return response containing success flag, GCPs with residuals, RMSE, and min
         *         points required
         * @throws IllegalArgumentException if imageId is null
         * @throws ImageNotFoundException   if the image doesn't exist
         * @throws GcpNotFoundException     if no GCPs exist for the image
         */
        @Transactional
        public ResidualsResponse updateResiduals(ResidualsRequest residualsRequest) {
                validateImageIdNotNull(residualsRequest.getImageId());

                GeorefImage image = imageRepository.findById(residualsRequest.getImageId())
                                .orElseThrow(() -> new ImageNotFoundException(
                                                "Image avec l'ID " + residualsRequest.getImageId() + " introuvable."));

                List<Gcp> gcps = getGcpsForImage(residualsRequest.getImageId());

                List<GcpDto> gcpDtos = GcpMapper.toGcpDtoList(gcps);

                int minPointsRequired = residualsService.getMinimumPointsRequired(residualsRequest.getType());

                // Check if sufficient GCPs exist for the transformation
                if (!residualsService.hasEnoughGCPs(gcpDtos, residualsRequest.getType())) {
                        List<Gcp> clearedGcps = clearResiduals(gcps);
                        return buildResponse(false, clearedGcps, null, minPointsRequired);
                }

                // Compute residuals using the residuals service
                ResidualsResult result = residualsService.computeResiduals(
                                gcpDtos,
                                residualsRequest.getType(),
                                residualsRequest.getSrid());

                // Update GCP entities with computed residuals
                List<Gcp> updatedGcps = updateResidualsInGcps(gcps, result.getResiduals());

                // Update image's mean residual (RMSE)
                updateMeanResidualForImage(image, result.getRmse());

                return buildResponse(true, updatedGcps, result.getRmse(), minPointsRequired);
        }

        /**
         * Loads (imports) multiple GCPs at once, with optional overwrite of existing
         * GCPs.
         * 
         * <p>
         * This method supports bulk GCP creation from external sources such as CSV
         * files,
         * text files, or API batch requests. It provides two loading modes:
         * </p>
         * 
         * <p>
         * <b>Loading Modes:</b>
         * </p>
         * <ul>
         * <li><b>Overwrite (overwrite=true):</b> Deletes all existing GCPs before
         * loading new ones</li>
         * <li><b>Append (overwrite=false):</b> Keeps existing GCPs and adds new ones
         * with incremented indices</li>
         * </ul>
         * 
         * <p>
         * <b>Index Assignment:</b>
         * </p>
         * <p>
         * Indices are automatically assigned sequentially starting from the next
         * available index
         * (or from 1 if overwrite is true). The indices in the input DTOs are ignored.
         * </p>
         * 
         * <p>
         * <b>Transaction Boundary:</b> Entire load operation is atomic. If any GCP
         * fails to save
         * (e.g., duplicate index, validation error), all changes are rolled back.
         * </p>
         * 
         * <p>
         * <b>Usage Example:</b>
         * </p>
         * 
         * <pre>{@code
         * // Load GCPs from CSV, replacing all existing ones
         * LoadGcpsRequest request = new LoadGcpsRequest(imageId, gcpList, true);
         * List<GcpDto> loaded = gcpService.loadGcps(request);
         * }</pre>
         * 
         * @param request the load request containing image ID, GCP list, and overwrite
         *                flag
         * @return list of saved GCPs with assigned IDs and indices
         * @throws IllegalArgumentException   if imageId is null or GCP list is empty
         * @throws ImageNotFoundException     if the image doesn't exist
         * @throws DuplicateGcpIndexException if an index conflict occurs (should not
         *                                    happen with auto-assignment)
         */
        @Transactional
        public List<GcpDto> loadGcps(LoadGcpsRequest request) {
                UUID imageId = request.getImageId();
                validateImageIdNotNull(imageId);

                if (request.getGcps() == null || request.getGcps().isEmpty()) {
                        throw new IllegalArgumentException("La liste des GCPs ne peut pas être vide.");
                }

                GeorefImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> new ImageNotFoundException(
                                                "Image avec l'ID " + imageId + " introuvable."));

                // If overwrite mode, delete all existing GCPs
                if (request.isOverwrite()) {
                        gcpRepository.deleteByImageId(imageId);
                }

                // Determine starting index
                Integer startIndex = gcpRepository.findMaxIndexByImageId(imageId)
                                .map(maxIndex -> maxIndex + 1)
                                .orElse(1);

                List<Gcp> newGcps = new ArrayList<>();
                Integer index = startIndex;

                // Create GCP entities with auto-assigned indices
                for (GcpDto dto : request.getGcps()) {
                        if (gcpRepository.existsByImageIdAndIndex(imageId, index)) {
                                throw new DuplicateGcpIndexException("Un GCP avec l'index " + index + " existe déjà.");
                        }

                        Gcp gcp = gcpFactory.createGcp(
                                        image,
                                        dto.getSourceX(),
                                        dto.getSourceY(),
                                        dto.getMapX(),
                                        dto.getMapY(),
                                        index++);
                        newGcps.add(gcp);
                }

                List<Gcp> savedGcps = gcpRepository.saveAll(newGcps);
                return GcpMapper.toGcpDtoList(savedGcps);
        }

        /**
         * Deletes all Ground Control Points associated with a specific image.
         * 
         * <p>
         * This is a bulk delete operation typically used when resetting georeferencing
         * for
         * an image or deleting the image itself (cascade delete).
         * </p>
         * 
         * <p>
         * <b>Idempotency:</b> Returns false if no GCPs exist, allowing the caller to
         * handle
         * the "not found" scenario appropriately (e.g., 404 vs. 204 in REST API).
         * </p>
         * 
         * <p>
         * <b>Transaction Boundary:</b> Entire deletion is atomic.
         * </p>
         * 
         * @param imageId the unique identifier of the georeferenced image
         * @return true if GCPs were deleted, false if no GCPs existed for the image
         */
        @Transactional
        public boolean deleteAllGcpsByImageId(UUID imageId) {
                List<Gcp> gcpList = gcpRepository.findAllByImageId(imageId);

                if (gcpList.isEmpty()) {
                        return false;
                }

                gcpRepository.deleteAll(gcpList);
                return true;
        }

        // ==================== PRIVATE HELPER METHODS ====================

        /**
         * Validates that the image ID is not null.
         * 
         * @param imageId the image ID to validate
         * @throws IllegalArgumentException if imageId is null
         */
        private void validateImageIdNotNull(UUID imageId) {
                if (imageId == null) {
                        throw new IllegalArgumentException("L'ID de l'image ne peut pas être null.");
                }
        }

        /**
         * Retrieves GCPs for an image and throws exception if none exist.
         * 
         * @param imageId the image ID
         * @return list of GCPs (guaranteed non-empty)
         * @throws GcpNotFoundException if no GCPs exist for the image
         */
        private List<Gcp> getGcpsForImage(UUID imageId) {
                List<Gcp> gcps = gcpRepository.findByImageId(imageId);
                if (gcps.isEmpty()) {
                        throw new GcpNotFoundException("Aucun GCP trouvé pour l'image avec l'ID : " + imageId);
                }
                return gcps;
        }

        /**
         * Clears residual values for all GCPs (sets to null).
         * 
         * <p>
         * Used when insufficient GCPs exist for residual computation, ensuring
         * stale residual values are not displayed.
         * </p>
         * 
         * @param gcps the list of GCPs to clear
         * @return the updated GCPs with null residuals
         */
        private List<Gcp> clearResiduals(List<Gcp> gcps) {
                gcps.forEach(gcp -> gcp.setResidual(null));
                List<Gcp> updatedGcps = gcpRepository.saveAll(gcps);
                return updatedGcps;
        }

        /**
         * Updates GCP entities with computed residual values.
         * 
         * <p>
         * Residuals are rounded to 4 decimal places for consistency. The order of
         * residuals
         * must match the order of GCPs in the input list.
         * </p>
         * 
         * @param gcps      the list of GCP entities to update
         * @param residuals the computed residual values (must have same size as gcps)
         * @return the updated GCP entities
         * @throws IllegalStateException if the sizes don't match
         */
        private List<Gcp> updateResidualsInGcps(List<Gcp> gcps, List<Double> residuals) {
                if (gcps.size() != residuals.size()) {
                        throw new IllegalStateException("Number of residuals does not match number of GCPs.");
                }
                for (int i = 0; i < gcps.size(); i++) {
                        double residual = Math.round(residuals.get(i) * 10000.0) / 10000.0;
                        gcps.get(i).setResidual(residual);
                }
                List<Gcp> updatedGcps = gcpRepository.saveAll(gcps);
                return updatedGcps;
        }

        /**
         * Updates the mean residual (RMSE) value in the image entity.
         * 
         * <p>
         * The RMSE is rounded to 4 decimal places and stored in the image for quick
         * access without recomputing residuals.
         * </p>
         * 
         * @param image        the georeferenced image to update
         * @param meanResidual the computed RMSE value
         */
        private void updateMeanResidualForImage(GeorefImage image, double meanResidual) {
                double roundedMeanResidual = Math.round(meanResidual * 10000.0) / 10000.0;
                image.setMeanResidual(roundedMeanResidual);
        }

        /**
         * Builds a standardized ResidualsResponse object.
         * 
         * @param success     whether residual computation succeeded
         * @param gcps        the GCP entities (with or without residuals)
         * @param rmse        the computed RMSE (null if success is false)
         * @param minRequired the minimum GCPs required for the transformation type
         * @return the constructed response
         */
        private ResidualsResponse buildResponse(boolean success, List<Gcp> gcps, Double rmse, int minRequired) {
                List<GcpDto> gcpDtos = GcpMapper.toGcpDtoList(gcps);
                return new ResidualsResponse(success, gcpDtos, rmse, minRequired);
        }
}