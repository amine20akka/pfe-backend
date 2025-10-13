package com.amine.pfe.georef_module.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.amine.pfe.georef_module.enums.Compression;
import com.amine.pfe.georef_module.enums.GeorefStatus;
import com.amine.pfe.georef_module.enums.ResamplingMethod;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;

/**
 * Entity representing an image in the georeferencing workflow.
 * 
 * <p>
 * This is the central entity of the georeferencing module, tracking an image
 * through
 * its entire lifecycle: upload, GCP collection, processing, and publication to
 * GeoServer.
 * </p>
 * 
 * <p>
 * <strong>Image Lifecycle:</strong>
 * </p>
 * <ol>
 * <li><strong>UPLOADED:</strong> Image uploaded, stored in originals
 * directory</li>
 * <li><strong>GCP_COLLECTION:</strong> User adds Ground Control Points</li>
 * <li><strong>PENDING:</strong> Ready for georeferencing, sent to RabbitMQ
 * queue</li>
 * <li><strong>PROCESSING:</strong> GDAL server is processing the image</li>
 * <li><strong>COMPLETED:</strong> Successfully georeferenced, saved in
 * georeferenced directory</li>
 * <li><strong>PUBLISHED:</strong> Published as a layer to GeoServer</li>
 * <li><strong>FAILED:</strong> Error occurred during processing</li>
 * </ol>
 * 
 * <p>
 * <strong>Database Schema:</strong>
 * </p>
 * <ul>
 * <li>Table: <code>georef_images</code></li>
 * <li>Schema: <code>georef</code></li>
 * </ul>
 * 
 * <p>
 * <strong>Relationships:</strong>
 * </p>
 * <ul>
 * <li>One-to-Many with {@link Gcp}: An image can have multiple Ground Control
 * Points</li>
 * <li>One-to-One with {@link GeorefLayer}: A georeferenced image can be
 * published as a GeoServer layer</li>
 * <li>All relationships use cascade operations and orphan removal for data
 * integrity</li>
 * </ul>
 * 
 * <p>
 * <strong>File Storage:</strong>
 * </p>
 * <ul>
 * <li>Original images: <code>./georef-storage/originals/</code></li>
 * <li>Georeferenced images: <code>./georef-storage/georeferenced/</code></li>
 * </ul>
 * 
 * <p>
 * <strong>Example Usage:</strong>
 * </p>
 * 
 * <pre>
 * GeorefImage image = GeorefImage.builder()
 *         .hash("a1b2c3d4e5f6...")
 *         .filepathOriginal("./georef-storage/originals/map_2024.tif")
 *         .uploadingDate(LocalDateTime.now())
 *         .status(GeorefStatus.UPLOADED)
 *         .srid(Srid._4326)
 *         .transformationType(TransformationType.POLYNOMIALE_1)
 *         .resamplingMethod(ResamplingMethod.BILINEAR)
 *         .compression(Compression.LZW)
 *         .build();
 * </pre>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see Gcp
 * @see GeorefLayer
 * @see GeorefStatus
 */
@Entity
@Table(name = "georef_images", schema = "georef")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeorefImage {

    /**
     * Unique identifier for the georeferencing image.
     * 
     * <p>
     * Generated automatically using UUID strategy. This ID is used to reference
     * the image across the entire system, including RabbitMQ messages and file
     * naming.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * SHA-256 hash of the original uploaded file.
     * 
     * <p>
     * Used for:
     * </p>
     * <ul>
     * <li>Detecting duplicate uploads (same file uploaded multiple times)</li>
     * <li>Verifying file integrity after upload</li>
     * <li>Ensuring data consistency across the system</li>
     * </ul>
     * 
     * <p>
     * Format: 64-character hexadecimal string
     * </p>
     * <p>
     * <strong>Required field</strong> - must be calculated and set on upload
     * </p>
     */
    @Column(nullable = false)
    private String hash;

    /**
     * File system path to the original uploaded image.
     * 
     * <p>
     * Points to the unmodified image stored in the originals directory.
     * This file is preserved for reference and potential reprocessing.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong>
     * <code>./georef-storage/originals/a1b2c3d4-e5f6-7890-abcd-ef1234567890.png</code>
     * </p>
     * 
     * <p>
     * <strong>Required field</strong> - set during upload process
     * </p>
     */
    @Column(nullable = false)
    private String filepathOriginal;

    /**
     * File system path to the georeferenced output image.
     * 
     * <p>
     * Points to the processed image in GeoTIFF format with embedded spatial
     * reference.
     * This file is created by the GDAL server after successful georeferencing.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong>
     * <code>./georef-storage/georeferenced/map_2024_georef.tif</code>
     * </p>
     * 
     * <p>
     * <strong>Nullable:</strong> Only populated after successful georeferencing
     * (status = COMPLETED)
     * </p>
     */
    private String filepathGeoreferenced;

    /**
     * Custom filename for the georeferenced output.
     * 
     * <p>
     * User-specified name for the final georeferenced file. If not provided,
     * a default name is generated based on the original filename.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong> <code>tunis_city_center_1950</code>
     * </p>
     * 
     * <p>
     * <strong>Nullable:</strong> Optional field, can be set by user or generated
     * automatically
     * </p>
     */
    private String outputFilename;

    /**
     * Timestamp when the image was uploaded to the system.
     * 
     * <p>
     * Recorded automatically during the upload process. Used for:
     * </p>
     * <ul>
     * <li>Tracking upload history</li>
     * <li>Sorting images by upload date</li>
     * <li>Auditing and analytics</li>
     * </ul>
     * 
     * <p>
     * <strong>Nullable:</strong> Should be set during upload, but nullable for data
     * migration scenarios
     * </p>
     */
    private LocalDateTime uploadingDate;

    /**
     * Timestamp of the most recent georeferencing operation.
     * 
     * <p>
     * Updated each time the image is reprocessed with new GCPs or different
     * parameters.
     * Useful for tracking processing history and versioning.
     * </p>
     * 
     * <p>
     * <strong>Nullable:</strong> Only set after the first georeferencing attempt
     * </p>
     */
    private LocalDateTime lastGeoreferencingDate;

    /**
     * The mathematical transformation method used for georeferencing.
     * 
     * <p>
     * Determines how GDAL calculates the mapping between source pixels and map
     * coordinates:
     * </p>
     * <ul>
     * <li><strong>POLYNOMIALE_1:</strong> First-order (affine) - for flat maps,
     * requires 3+ GCPs</li>
     * <li><strong>POLYNOMIALE_2:</strong> Second-order - handles curved surfaces,
     * requires 6+ GCPs</li>
     * <li><strong>POLYNOMIALE_3:</strong> Third-order - complex distortions,
     * requires 10+ GCPs</li>
     * </ul>
     * 
     * <p>
     * <strong>Nullable:</strong> Set by user before georeferencing, may be null
     * during GCP collection
     * </p>
     * 
     * @see TransformationType
     */
    @Enumerated(EnumType.STRING)
    private TransformationType transformationType;

    /**
     * Spatial Reference Identifier (SRID) for the target coordinate system.
     * 
     * <p>
     * Defines the coordinate reference system (CRS) of the map coordinates (mapX,
     * mapY in GCPs).
     * Common values:
     * </p>
     * <ul>
     * <li><strong>EPSG:4326</strong> - WGS84 Geographic (latitude/longitude)</li>
     * <li><strong>EPSG:3857</strong> - Web Mercator (used by Google Maps,
     * OpenStreetMap)</li>
     * </ul>
     * 
     * <p>
     * The output georeferenced image will be in this coordinate system.
     * </p>
     * 
     * <p>
     * <strong>Nullable:</strong> Must be set before georeferencing
     * </p>
     * 
     * @see Srid
     */
    @Enumerated(EnumType.STRING)
    private Srid srid;

    /**
     * Current processing status of the image in the georeferencing workflow.
     * 
     * <p>
     * Status progression:
     * </p>
     * <ul>
     * <li><strong>UPLOADED:</strong> Initial state after upload</li>
     * <li><strong>PENDING:</strong> Ready for processing, in RabbitMQ queue</li>
     * <li><strong>PROCESSING:</strong> GDAL server is georeferencing the image</li>
     * <li><strong>COMPLETED:</strong> Successfully georeferenced</li>
     * <li><strong>FAILED:</strong> Processing error occurred</li>
     * </ul>
     * 
     * <p>
     * <strong>Nullable:</strong> Should always have a status, but nullable for
     * flexibility
     * </p>
     * 
     * @see GeorefStatus
     */
    @Enumerated(EnumType.STRING)
    private GeorefStatus status;

    /**
     * Resampling method for pixel interpolation during georeferencing.
     * 
     * <p>
     * Determines how pixel values are calculated when transforming the image:
     * </p>
     * <ul>
     * <li><strong>NEAREST:</strong> Fast, preserves original values,
     * blocky output</li>
     * <li><strong>BILINEAR:</strong> Balanced quality/speed, smooth output</li>
     * <li><strong>CUBIC:</strong> High quality, slower, very smooth output</li>
     * </ul>
     * 
     * <p>
     * Recommended: BILINEAR for most use cases, NEAREST for categorical
     * data (land use maps)
     * </p>
     * 
     * <p>
     * <strong>Nullable:</strong> Can use GDAL default if not specified
     * </p>
     * 
     * @see ResamplingMethod
     */
    @Enumerated(EnumType.STRING)
    private ResamplingMethod resamplingMethod;

    /**
     * Compression method for the georeferenced output GeoTIFF.
     * 
     * <p>
     * Reduces file size while maintaining image quality:
     * </p>
     * <ul>
     * <li><strong>NONE:</strong> No compression, largest file size, fastest
     * processing</li>
     * <li><strong>LZW:</strong> Lossless compression, good balance
     * (recommended)</li>
     * <li><strong>DEFLATE:</strong> Lossless, better compression than LZW,
     * slower</li>
     * <li><strong>JPEG:</strong> Lossy compression, smallest file size, for aerial
     * photos</li>
     * </ul>
     * 
     * <p>
     * <strong>Nullable:</strong> Can use GDAL default (usually LZW) if not
     * specified
     * </p>
     * 
     * @see Compression
     */
    @Enumerated(EnumType.STRING)
    private Compression compression;

    /**
     * Average residual error across all Ground Control Points.
     * 
     * <p>
     * Calculated as the mean of all individual GCP residuals after transformation.
     * Provides an overall measure of georeferencing accuracy:
     * </p>
     * <ul>
     * <li>Mean residual &lt; 1 pixel: Excellent accuracy</li>
     * <li>Mean residual 1-2 pixels: Good accuracy</li>
     * <li>Mean residual 2-3 pixels: Acceptable for most purposes</li>
     * <li>Mean residual &gt; 3 pixels: Review GCP placement or add more GCPs</li>
     * </ul>
     * 
     * <p>
     * <strong>Nullable:</strong> Only calculated after successful georeferencing
     * </p>
     * 
     * <p>
     * Unit: Pixels of the source image
     * </p>
     */
    private Double meanResidual;

    /**
     * Collection of Ground Control Points associated with this image.
     * 
     * <p>
     * The GCPs define the correspondence between source image coordinates and
     * real-world map coordinates. Used by GDAL to calculate the transformation.
     * </p>
     * 
     * <p>
     * <strong>Cascade Operations:</strong>
     * </p>
     * <ul>
     * <li>ALL: All operations (persist, merge, remove, refresh, detach) cascade to
     * GCPs</li>
     * <li>orphanRemoval = true: GCPs removed from this list are deleted from
     * database</li>
     * </ul>
     * 
     * <p>
     * <strong>Minimum Requirements:</strong>
     * </p>
     * <ul>
     * <li>POLYNOMIALE_1: At least 3 GCPs</li>
     * <li>POLYNOMIALE_2: At least 6 GCPs</li>
     * <li>POLYNOMIALE_3: At least 10 GCPs</li>
     * <li>More GCPs generally improve accuracy</li>
     * </ul>
     * 
     * @see Gcp
     */
    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gcp> gcps;

    /**
     * GeoServer layer information for this georeferenced image.
     * 
     * <p>
     * Created when the georeferenced image is published to GeoServer for web
     * mapping.
     * Contains all necessary information to access the layer via WMS/WFS services.
     * </p>
     * 
     * <p>
     * <strong>Cascade Operations:</strong>
     * </p>
     * <ul>
     * <li>ALL: All operations cascade to the layer</li>
     * <li>orphanRemoval = true: Layer is deleted if disassociated from image</li>
     * </ul>
     * 
     * <p>
     * <strong>Nullable:</strong> Only populated after successful publication to
     * GeoServer (status = COMPLETED)
     * </p>
     * 
     * @see GeorefLayer
     */
    @OneToOne(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private GeorefLayer layer;
}