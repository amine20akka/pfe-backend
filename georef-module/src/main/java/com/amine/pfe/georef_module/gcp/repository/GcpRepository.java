package com.amine.pfe.georef_module.gcp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amine.pfe.georef_module.entity.Gcp;

/**
 * Spring Data JPA repository for managing Ground Control Point (GCP) entities.
 * 
 * <p>
 * This repository provides data access operations for GCPs, which are critical
 * reference
 * points used in image georeferencing. It extends {@link JpaRepository} to
 * inherit standard
 * CRUD operations and defines custom query methods for GCP-specific retrieval
 * patterns.
 * </p>
 * 
 * <p>
 * <b>Primary Use Cases:</b>
 * </p>
 * <ul>
 * <li>Retrieving all GCPs associated with a specific georeferenced image</li>
 * <li>Checking for duplicate GCP indices within an image</li>
 * <li>Fetching GCPs in ordered sequence for transformation calculations</li>
 * <li>Managing GCP lifecycle (create, update, delete) per image</li>
 * </ul>
 * 
 * <p>
 * <b>Repository Pattern Benefits:</b>
 * </p>
 * <ul>
 * <li>Abstracts data access logic from business services</li>
 * <li>Provides type-safe query methods through Spring Data conventions</li>
 * <li>Automatic implementation generation by Spring Data JPA</li>
 * <li>Transaction management handled by Spring</li>
 * </ul>
 * 
 * <p>
 * <b>Key Relationships:</b>
 * </p>
 * <ul>
 * <li>GCP → GeorefImage: Many-to-One (multiple GCPs belong to one image)</li>
 * <li>Each GCP has a unique index within its parent image</li>
 * </ul>
 * 
 * <p>
 * <b>Transaction Considerations:</b>
 * </p>
 * <p>
 * All repository methods inherit default transactional behavior from Spring
 * Data JPA.
 * Modifying operations (save, delete) should be called within a service layer
 * transaction
 * for proper rollback support.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @see Gcp
 * @see JpaRepository
 */
public interface GcpRepository extends JpaRepository<Gcp, UUID> {

    /**
     * Retrieves all Ground Control Points associated with a specific georeferenced
     * image.
     * 
     * <p>
     * This method uses Spring Data JPA's query derivation mechanism to
     * automatically
     * generate a query based on the method name pattern {@code findBy[Property]}.
     * </p>
     * 
     * <p>
     * <b>Generated Query (equivalent):</b>
     * </p>
     * 
     * <pre>
     * SELECT g FROM Gcp g WHERE g.image.id = :imageId
     * </pre>
     * 
     * <p>
     * <b>Use Case:</b> Fetch all GCPs for residual computation, transformation
     * fitting,
     * or display in the UI for a specific image.
     * </p>
     * 
     * <p>
     * <b>Note:</b> The returned list is unordered. For ordered retrieval, use
     * {@link #findAllByImageIdOrderByIndex(UUID)} instead.
     * </p>
     * 
     * @param imageId the unique identifier of the georeferenced image
     * @return list of all GCPs belonging to the specified image (empty list if none
     *         exist)
     * @see #findAllByImageIdOrderByIndex(UUID)
     */
    List<Gcp> findByImageId(UUID imageId);

    /**
     * Checks whether a GCP with a specific index already exists for a given image.
     * 
     * <p>
     * This method is crucial for enforcing unique index constraints within an
     * image's
     * GCP collection. It should be called before creating or updating a GCP to
     * prevent
     * duplicate indices.
     * </p>
     * 
     * <p>
     * <b>Generated Query (equivalent):</b>
     * </p>
     * 
     * <pre>
     * SELECT COUNT(g) > 0 FROM Gcp g WHERE g.image.id = :imageId AND g.index = :index
     * </pre>
     * 
     * <p>
     * <b>Use Case:</b>
     * </p>
     * 
     * <pre>{@code
     * // Validate before creating a new GCP
     * if (gcpRepository.existsByImageIdAndIndex(imageId, newIndex)) {
     *     throw new DuplicateGcpIndexException("GCP with index " + newIndex + " already exists");
     * }
     * }</pre>
     * 
     * <p>
     * <b>Performance:</b> This method is optimized to return a boolean without
     * fetching
     * the entire entity, making it more efficient than retrieving and checking.
     * </p>
     * 
     * @param imageId the unique identifier of the georeferenced image
     * @param index   the GCP index to check for existence
     * @return true if a GCP with the specified index exists for this image, false
     *         otherwise
     */
    boolean existsByImageIdAndIndex(UUID imageId, int index);

    /**
     * Retrieves all Ground Control Points for an image, sorted by their index in
     * ascending order.
     * 
     * <p>
     * This method combines filtering and sorting in a single query, ensuring GCPs
     * are
     * returned in their logical sequence. This ordering is essential for:
     * </p>
     * <ul>
     * <li>Displaying GCPs in the UI in a consistent, predictable order</li>
     * <li>Processing GCPs sequentially in algorithms</li>
     * <li>Matching GCPs with their corresponding residuals in analysis results</li>
     * </ul>
     * 
     * <p>
     * <b>Generated Query (equivalent):</b>
     * </p>
     * 
     * <pre>
     * SELECT g FROM Gcp g WHERE g.image.id = :imageId ORDER BY g.index ASC
     * </pre>
     * 
     * <p>
     * <b>Use Case:</b>
     * </p>
     * 
     * <pre>{@code
     * // Retrieve GCPs for residual computation in consistent order
     * List<Gcp> gcps = gcpRepository.findAllByImageIdOrderByIndex(imageId);
     * ResidualsResult result = residualsService.computeResiduals(
     *         gcps.stream().map(GcpMapper::toDto).collect(Collectors.toList()),
     *         TransformationType.POLYNOMIALE_2,
     *         image.getSrid());
     * }</pre>
     * 
     * <p>
     * <b>Ordering Guarantee:</b> GCPs are always returned sorted by index (1, 2, 3,
     * ...).
     * </p>
     * 
     * @param imageId the unique identifier of the georeferenced image
     * @return list of GCPs ordered by their index (empty list if none exist)
     * @see #findByImageId(UUID)
     */
    List<Gcp> findAllByImageIdOrderByIndex(UUID imageId);

    /**
     * Finds the maximum GCP index currently used for a specific image.
     * 
     * <p>
     * This method uses a custom JPQL query with an aggregate function to
     * efficiently
     * determine the highest index value without retrieving all GCPs. It's
     * particularly
     * useful for auto-generating the next available index when creating a new GCP.
     * </p>
     * 
     * <p>
     * <b>JPQL Query:</b>
     * </p>
     * 
     * <pre>
     * SELECT MAX(g.index) FROM Gcp g WHERE g.image.id = :imageId
     * </pre>
     * 
     * <p>
     * <b>Return Behavior:</b>
     * </p>
     * <ul>
     * <li>Returns {@code Optional.of(maxIndex)} if GCPs exist for the image</li>
     * <li>Returns {@code Optional.empty()} if no GCPs exist yet</li>
     * </ul>
     * 
     * <p>
     * <b>Use Case:</b>
     * </p>
     * 
     * <pre>{@code
     * // Auto-generate next index for a new GCP
     * int nextIndex = gcpRepository.findMaxIndexByImageId(imageId)
     *         .map(max -> max + 1) // Increment if GCPs exist
     *         .orElse(1); // Start at 1 if no GCPs exist
     * 
     * Gcp newGcp = gcpFactory.createGcp(image, sourceX, sourceY, mapX, mapY, nextIndex);
     * }</pre>
     * 
     * <p>
     * <b>Performance:</b> This is an O(1) aggregate query that doesn't load full
     * entities,
     * making it efficient even for images with many GCPs.
     * </p>
     * 
     * @param imageId the unique identifier of the georeferenced image
     * @return Optional containing the maximum index if GCPs exist, empty Optional
     *         otherwise
     */
    @Query("SELECT MAX(g.index) FROM Gcp g WHERE g.image.id = :imageId")
    Optional<Integer> findMaxIndexByImageId(@Param("imageId") UUID imageId);

    /**
     * Deletes all Ground Control Points associated with a specific georeferenced
     * image.
     * 
     * <p>
     * This is a bulk delete operation that removes all GCPs belonging to an image
     * in a
     * single query. It's typically used when:
     * </p>
     * <ul>
     * <li>Deleting a georeferenced image (cascade delete GCPs)</li>
     * <li>Resetting all GCPs to start georeferencing from scratch</li>
     * <li>Cleaning up after a failed georeferencing attempt</li>
     * </ul>
     * 
     * <p>
     * <b>Generated Query (equivalent):</b>
     * </p>
     * 
     * <pre>
     * DELETE FROM Gcp g WHERE g.image.id = :imageId
     * </pre>
     * 
     * <p>
     * <b>Important Considerations:</b>
     * </p>
     * <ul>
     * <li><b>Transaction Required:</b> This method MUST be called within an active
     * transaction.
     * Annotate the calling service method with {@code @Transactional}.</li>
     * <li><b>Cascade Behavior:</b> Ensure this aligns with your JPA entity cascade
     * settings.</li>
     * <li><b>No Return Value:</b> Method is void; doesn't return the number of
     * deleted records.</li>
     * </ul>
     * 
     * 
     * @param imageId the unique identifier of the georeferenced image whose GCPs
     *                should be deleted
     */
    void deleteByImageId(UUID imageId);

    /**
     * Retrieves all Ground Control Points for a specific image (unordered).
     * 
     * <p>
     * <b>Note:</b> This method is functionally equivalent to
     * {@link #findByImageId(UUID)}.
     * Having both methods may indicate a refactoring opportunity to consolidate to
     * a single
     * method name for consistency.
     * </p>
     * 
     * <p>
     * <b>Recommendation:</b> Use {@link #findAllByImageIdOrderByIndex(UUID)} for
     * most use
     * cases to ensure consistent ordering, or {@link #findByImageId(UUID)} as the
     * standard
     * unordered retrieval method.
     * </p>
     * 
     * @param imageId the unique identifier of the georeferenced image
     * @return list of all GCPs belonging to the specified image (empty list if none
     *         exist)
     * @see #findByImageId(UUID)
     * @see #findAllByImageIdOrderByIndex(UUID)
     */
    List<Gcp> findAllByImageId(UUID imageId);
}