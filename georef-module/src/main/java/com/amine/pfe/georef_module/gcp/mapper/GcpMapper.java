package com.amine.pfe.georef_module.gcp.mapper;

import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.entity.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between GCP entities and DTOs.
 * 
 * <p>
 * This mapper implements the Data Transfer Object (DTO) pattern by providing
 * bidirectional
 * conversion between {@link Gcp} JPA entities (persistence layer) and
 * {@link GcpDto} objects
 * (presentation/service layer). This separation ensures clean architecture by
 * decoupling
 * the internal domain model from external APIs and UI representations.
 * </p>
 * 
 * <p>
 * <b>Key Responsibilities:</b>
 * </p>
 * <ul>
 * <li>Convert GCP entities to DTOs for API responses and inter-service
 * communication</li>
 * <li>Convert DTOs to entities for persistence operations</li>
 * <li>Handle bulk conversions for collections of GCPs</li>
 * <li>Maintain consistent mapping logic across the application</li>
 * </ul>
 * 
 * <p>
 * <b>Architecture Benefits:</b>
 * </p>
 * <ul>
 * <li><b>Layer Separation:</b> Persistence entities don't leak into API
 * responses</li>
 * <li><b>API Stability:</b> Internal entity changes don't break API
 * contracts</li>
 * <li><b>Security:</b> Prevents accidental exposure of internal entity
 * relationships</li>
 * <li><b>Performance:</b> DTOs can be optimized for specific use cases (e.g.,
 * excluding lazy-loaded relations)</li>
 * </ul>
 * 
 * <p>
 * <b>Design Pattern:</b>
 * </p>
 * <p>
 * This is a stateless utility class with static methods, following the Mapper
 * pattern.
 * All methods are pure functions with no side effects, making them thread-safe
 * and predictable.
 * </p>
 * 
 * <p>
 * <b>Usage Context:</b>
 * </p>
 * 
 * <pre>
 * {@code
 * // In a REST controller
 * &#64;GetMapping("/{imageId}/gcps")
 * public List<GcpDto> getGcps(@PathVariable UUID imageId) {
 *     List<Gcp> entities = gcpRepository.findAllByImageIdOrderByIndex(imageId);
 *     return GcpMapper.toGcpDtoList(entities);  // Convert for API response
 * }
 * 
 * // In a service layer
 * @Transactional
 * public GcpDto createGcp(UUID imageId, GcpDto dto) {
 *     GeorefImage image = imageRepository.findById(imageId)
 *         .orElseThrow(() -> new ImageNotFoundException(imageId));
 *     Gcp entity = GcpMapper.toEntity(dto, image);  // Convert for persistence
 *     Gcp saved = gcpRepository.save(entity);
 *     return GcpMapper.toDto(saved);  // Convert back for response
 * }
 * }
 * </pre>
 * 
 * @author Amine
 * @version 1.0
 * @see Gcp
 * @see GcpDto
 * @see GeorefImage
 */
public class GcpMapper {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * 
     * <p>
     * This class contains only static methods and should never be instantiated.
     * The private constructor enforces this design constraint.
     * </p>
     */
    private GcpMapper() {
        throw new UnsupportedOperationException("GcpMapper is a utility class and cannot be instantiated");
    }

    // ==================== ENTITY TO DTO CONVERSIONS ====================

    /**
     * Converts a GCP entity to its corresponding DTO representation.
     * 
     * <p>
     * This method extracts all relevant data from the JPA entity and constructs a
     * lightweight DTO suitable for serialization to JSON in REST API responses. The
     * DTO
     * includes only the image ID reference (not the full image object) to prevent
     * circular
     * references and unnecessary data transfer.
     * </p>
     * 
     * <p>
     * <b>Mapped Fields:</b>
     * </p>
     * <table border="1">
     * <tr>
     * <th>Entity Field</th>
     * <th>DTO Field</th>
     * <th>Notes</th>
     * </tr>
     * <tr>
     * <td>id</td>
     * <td>id</td>
     * <td>Primary key (UUID)</td>
     * </tr>
     * <tr>
     * <td>image</td>
     * <td>imageId</td>
     * <td>Only ID extracted (avoids lazy loading issues)</td>
     * </tr>
     * <tr>
     * <td>sourceX</td>
     * <td>sourceX</td>
     * <td>Image pixel X-coordinate</td>
     * </tr>
     * <tr>
     * <td>sourceY</td>
     * <td>sourceY</td>
     * <td>Image pixel Y-coordinate</td>
     * </tr>
     * <tr>
     * <td>mapX</td>
     * <td>mapX</td>
     * <td>Map X-coordinate (Easting/Longitude)</td>
     * </tr>
     * <tr>
     * <td>mapY</td>
     * <td>mapY</td>
     * <td>Map Y-coordinate (Northing/Latitude)</td>
     * </tr>
     * <tr>
     * <td>index</td>
     * <td>index</td>
     * <td>Sequential GCP identifier</td>
     * </tr>
     * <tr>
     * <td>residual</td>
     * <td>residual</td>
     * <td>Computed accuracy metric (may be null)</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <b>Thread Safety:</b> This method is stateless and thread-safe.
     * </p>
     * 
     * <p>
     * <b>Null Handling:</b> This method assumes the entity and its image
     * relationship
     * are not null. If there's a possibility of null values, add validation:
     * </p>
     * 
     * <pre>{@code
     * if (entity == null || entity.getImage() == null) {
     *     throw new IllegalArgumentException("Entity and image must not be null");
     * }
     * }</pre>
     * 
     * @param entity the GCP entity to convert (must not be null, and must have a
     *               non-null image)
     * @return the corresponding GcpDto with all fields populated
     * @throws NullPointerException if entity or entity.getImage() is null
     * @see #toEntity(GcpDto, GeorefImage)
     * @see #toGcpDtoList(List)
     */
    public static GcpDto toDto(Gcp entity) {
        return GcpDto.builder()
                .id(entity.getId())
                .imageId(entity.getImage().getId())
                .sourceX(entity.getSourceX())
                .sourceY(entity.getSourceY())
                .mapX(entity.getMapX())
                .mapY(entity.getMapY())
                .index(entity.getIndex())
                .residual(entity.getResidual())
                .build();
    }

    /**
     * Converts a list of GCP entities to a list of DTOs.
     * 
     * <p>
     * This is a convenience method for bulk conversion, commonly used when
     * retrieving
     * all GCPs for an image to return in an API response. It uses Java Streams for
     * efficient functional-style transformation.
     * </p>
     * 
     * <p>
     * <b>Implementation:</b> Leverages {@link #toDto(Gcp)} for each entity via
     * method reference.
     * </p>
     * 
     * <p>
     * <b>Empty List Handling:</b> Returns an empty list if the input is empty (not
     * null).
     * </p>
     * 
     * <p>
     * <b>Performance:</b>
     * </p>
     * <ul>
     * <li>O(n) time complexity where n is the number of GCPs</li>
     * <li>Creates a new list; does not modify the input</li>
     * <li>Stream operations are lazy until terminal operation (collect) is
     * called</li>
     * </ul>
     * 
     * <p>
     * <b>Usage Example:</b>
     * </p>
     * 
     * <pre>{@code
     * List<Gcp> entities = gcpRepository.findAllByImageIdOrderByIndex(imageId);
     * List<GcpDto> dtos = GcpMapper.toGcpDtoList(entities);
     * return ResponseEntity.ok(dtos); // Return as JSON array
     * }</pre>
     * 
     * @param gcps the list of GCP entities to convert (must not be null, can be
     *             empty)
     * @return a new list containing DTOs for all input entities
     * @throws NullPointerException if gcps is null or any entity/image within is
     *                              null
     * @see #toDto(Gcp)
     */
    public static List<GcpDto> toGcpDtoList(List<Gcp> gcps) {
        return gcps.stream()
                .map(GcpMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== DTO TO ENTITY CONVERSIONS ====================

    /**
     * Converts a GCP DTO to its corresponding entity representation.
     * 
     * <p>
     * This method reconstructs a JPA entity from a DTO, establishing the
     * bidirectional
     * relationship with the provided {@link GeorefImage}. This is typically used
     * when
     * creating or updating GCPs from API requests.
     * </p>
     * 
     * <p>
     * <b>Important Design Decision:</b>
     * </p>
     * <p>
     * The {@code image} parameter must be provided separately (not fetched from the
     * DTO's imageId)
     * because:
     * </p>
     * <ul>
     * <li>The image should be fetched once at the service layer (not repeatedly in
     * the mapper)</li>
     * <li>Allows proper transaction management and validation before mapping</li>
     * <li>Prevents unnecessary database queries within the mapping layer</li>
     * <li>The caller has control over image retrieval strategy (with/without locks,
     * etc.)</li>
     * </ul>
     * 
     * <p>
     * <b>Mapped Fields:</b>
     * </p>
     * <table border="1">
     * <tr>
     * <th>DTO Field</th>
     * <th>Entity Field</th>
     * <th>Source</th>
     * </tr>
     * <tr>
     * <td>id</td>
     * <td>id</td>
     * <td>From DTO</td>
     * </tr>
     * <tr>
     * <td>imageId</td>
     * <td>image</td>
     * <td>From parameter (not DTO)</td>
     * </tr>
     * <tr>
     * <td>sourceX</td>
     * <td>sourceX</td>
     * <td>From DTO</td>
     * </tr>
     * <tr>
     * <td>sourceY</td>
     * <td>sourceY</td>
     * <td>From DTO</td>
     * </tr>
     * <tr>
     * <td>mapX</td>
     * <td>mapX</td>
     * <td>From DTO</td>
     * </tr>
     * <tr>
     * <td>mapY</td>
     * <td>mapY</td>
     * <td>From DTO</td>
     * </tr>
     * <tr>
     * <td>index</td>
     * <td>index</td>
     * <td>From DTO</td>
     * </tr>
     * <tr>
     * <td>residual</td>
     * <td>residual</td>
     * <td>From DTO</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <b>Usage Example:</b>
     * </p>
     * 
     * <pre>{@code
     * @Transactional
     * public GcpDto updateGcp(UUID imageId, UUID gcpId, GcpDto dto) {
     *     // Fetch image first (with proper error handling)
     *     GeorefImage image = imageRepository.findById(imageId)
     *             .orElseThrow(() -> new ImageNotFoundException(imageId));
     * 
     *     // Convert DTO to entity with explicit image reference
     *     Gcp entity = GcpMapper.toEntity(dto, image);
     * 
     *     // Persist and return
     *     Gcp saved = gcpRepository.save(entity);
     *     return GcpMapper.toDto(saved);
     * }
     * }</pre>
     * 
     * <p>
     * <b>Note on ID Mapping:</b> The DTO's ID is preserved in the entity. For new
     * GCPs,
     * the DTO ID should be null, allowing JPA to generate a new UUID on persist.
     * </p>
     * 
     * @param dto   the GCP DTO to convert (must not be null)
     * @param image the GeorefImage entity this GCP belongs to (must not be null,
     *              should be managed/attached)
     * @return a new GCP entity with all fields populated and the image relationship
     *         established
     * @throws NullPointerException if dto or image is null
     * @see #toDto(Gcp)
     * @see #toGcpEntityList(List, GeorefImage)
     */
    public static Gcp toEntity(GcpDto dto, GeorefImage image) {
        Gcp entity = new Gcp();
        entity.setId(dto.getId());
        entity.setImage(image);
        entity.setSourceX(dto.getSourceX());
        entity.setSourceY(dto.getSourceY());
        entity.setMapX(dto.getMapX());
        entity.setMapY(dto.getMapY());
        entity.setIndex(dto.getIndex());
        entity.setResidual(dto.getResidual());
        return entity;
    }

    /**
     * Converts a list of GCP DTOs to a list of entities.
     * 
     * <p>
     * This bulk conversion method is useful when processing multiple GCP updates or
     * imports from external sources. All resulting entities will be associated with
     * the
     * same {@code image} parameter.
     * </p>
     * 
     * <p>
     * <b>Implementation:</b> Uses Java Streams with a lambda that partially applies
     * {@link #toEntity(GcpDto, GeorefImage)} with the provided image.
     * </p>
     * 
     * <p>
     * <b>Performance:</b>
     * </p>
     * <ul>
     * <li>O(n) time complexity where n is the number of DTOs</li>
     * <li>Creates a new list of entities</li>
     * <li>All entities reference the same image instance (memory efficient)</li>
     * </ul>
     * 
     * <p>
     * <b>Usage Example:</b>
     * </p>
     * 
     * <pre>{@code
     * @Transactional
     * public List<GcpDto> importGcps(UUID imageId, List<GcpDto> dtos) {
     *     GeorefImage image = imageRepository.findById(imageId)
     *             .orElseThrow(() -> new ImageNotFoundException(imageId));
     * 
     *     // Convert all DTOs to entities at once
     *     List<Gcp> entities = GcpMapper.toGcpEntityList(dtos, image);
     * 
     *     // Batch save for efficiency
     *     List<Gcp> saved = gcpRepository.saveAll(entities);
     * 
     *     return GcpMapper.toGcpDtoList(saved);
     * }
     * }</pre>
     * 
     * <p>
     * <b>Validation Consideration:</b> This method does not validate that the DTOs'
     * {@code imageId} fields match the provided {@code image.getId()}. The caller
     * should
     * ensure consistency or add validation:
     * </p>
     * 
     * <pre>{@code
     * gcpDtos.forEach(dto -> {
     *     if (!dto.getImageId().equals(imageId)) {
     *         throw new IllegalArgumentException("DTO imageId mismatch");
     *     }
     * });
     * }</pre>
     * 
     * @param gcpDtos the list of GCP DTOs to convert (must not be null, can be
     *                empty)
     * @param image   the GeorefImage entity all GCPs belong to (must not be null)
     * @return a new list containing entities for all input DTOs, all associated
     *         with the provided image
     * @throws NullPointerException if gcpDtos or image is null, or any DTO within
     *                              is null
     * @see #toEntity(GcpDto, GeorefImage)
     */
    public static List<Gcp> toGcpEntityList(List<GcpDto> gcpDtos, GeorefImage image) {
        return gcpDtos.stream()
                .map(gcpDto -> toEntity(gcpDto, image))
                .collect(Collectors.toList());
    }
}