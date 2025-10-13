package com.amine.pfe.georef_module.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Ground Control Point (GCP) used in the georeferencing
 * process.
 * 
 * <p>
 * A Ground Control Point is a correspondence between a pixel location in the
 * source image
 * (sourceX, sourceY) and its real-world geographic coordinates (mapX, mapY).
 * Multiple GCPs
 * are used to establish the spatial transformation that georeferences an image.
 * </p>
 * 
 * <p>
 * <strong>Georeferencing Process:</strong>
 * </p>
 * <ol>
 * <li>User identifies recognizable features in the uploaded image</li>
 * <li>User marks the pixel coordinates (sourceX, sourceY) of these
 * features</li>
 * <li>User provides the corresponding geographic coordinates (mapX, mapY)</li>
 * <li>GDAL uses these GCP pairs to calculate the transformation matrix</li>
 * <li>The transformation is applied to georeference the entire image</li>
 * </ol>
 * 
 * <p>
 * <strong>Database Schema:</strong>
 * </p>
 * <ul>
 * <li>Table: <code>gcp</code></li>
 * <li>Schema: <code>georef</code></li>
 * <li>Unique Constraint: Each image can have only one GCP with a given
 * index</li>
 * </ul>
 * 
 * <p>
 * <strong>Relationships:</strong>
 * </p>
 * <ul>
 * <li>Many GCPs belong to one {@link GeorefImage}</li>
 * <li>Typical georeferencing requires 4-6 GCPs for accuracy</li>
 * <li>Cascade deletion: When a GeorefImage is deleted, all its GCPs are
 * deleted</li>
 * </ul>
 * 
 * <p>
 * <strong>Example Usage:</strong>
 * </p>
 * 
 * <pre>
 * // Creating a GCP for a building corner
 * Gcp gcp = Gcp.builder()
 *         .image(geoRefImage)
 *         .sourceX(450.5) // Pixel X in original image
 *         .sourceY(320.8) // Pixel Y in original image
 *         .mapX(10.1815) // Longitude (e.g., Tunis)
 *         .mapY(36.8065) // Latitude (e.g., Tunis)
 *         .index(1) // First GCP
 *         .build();
 * </pre>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see GeorefImage
 */
@Entity
@Table(name = "gcp", schema = "georef", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "image_id", "index" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gcp {

    /**
     * Unique identifier for the Ground Control Point.
     * 
     * <p>
     * Generated automatically using UUID strategy to ensure global uniqueness
     * across distributed systems or database migrations.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * The georeferencing image to which this GCP belongs.
     * 
     * <p>
     * Establishes a many-to-one relationship where multiple GCPs can be associated
     * with a single image. This is a required field (nullable = false) as every GCP
     * must be linked to an image.
     * </p>
     * 
     * <p>
     * Database: Foreign key to the <code>georef_images</code> table.
     * </p>
     * 
     * @see GeorefImage
     */
    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private GeorefImage image;

    /**
     * X-coordinate (horizontal) of the point in the source image pixel space.
     * 
     * <p>
     * Represents the column position in the original, non-georeferenced image.
     * Origin is typically at the top-left corner of the image.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong> If a building corner is at column 450.5 in the
     * image,
     * sourceX = 450.5
     * </p>
     * 
     * <p>
     * Type: Double to support sub-pixel precision for accurate georeferencing.
     * </p>
     */
    @Column(nullable = false)
    private Double sourceX;

    /**
     * Y-coordinate (vertical) of the point in the source image pixel space.
     * 
     * <p>
     * Represents the row position in the original, non-georeferenced image.
     * Origin is typically at the top-left corner of the image.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong> If a building corner is at row 320.8 in the image,
     * sourceY = 320.8
     * </p>
     * 
     * <p>
     * Type: Double to support sub-pixel precision for accurate georeferencing.
     * </p>
     */
    @Column(nullable = false)
    private Double sourceY;

    /**
     * X-coordinate (longitude or easting) in the target map coordinate system.
     * 
     * <p>
     * Represents the real-world geographic position corresponding to the source
     * point.
     * The coordinate system depends on the target CRS (Coordinate Reference
     * System):
     * </p>
     * <ul>
     * <li><strong>Geographic (WGS84):</strong> Longitude in decimal degrees (e.g.,
     * 10.1815 for Tunis)</li>
     * <li><strong>Projected (UTM):</strong> Easting in meters (e.g., 500000)</li>
     * </ul>
     * 
     * <p>
     * This value is used by GDAL to calculate the spatial transformation.
     * </p>
     */
    @Column(nullable = false)
    private Double mapX;

    /**
     * Y-coordinate (latitude or northing) in the target map coordinate system.
     * 
     * <p>
     * Represents the real-world geographic position corresponding to the source
     * point.
     * The coordinate system depends on the target CRS (Coordinate Reference
     * System):
     * </p>
     * <ul>
     * <li><strong>Geographic (WGS84):</strong> Latitude in decimal degrees (e.g.,
     * 36.8065 for Tunis)</li>
     * <li><strong>Projected (UTM):</strong> Northing in meters (e.g., 4000000)</li>
     * </ul>
     * 
     * <p>
     * This value is used by GDAL to calculate the spatial transformation.
     * </p>
     */
    @Column(nullable = false)
    private Double mapY;

    /**
     * Sequential index of this GCP within the parent image.
     * 
     * <p>
     * Used to order and identify GCPs during the georeferencing process.
     * Each image must have unique indices starting from 0.
     * </p>
     * 
     * <p>
     * <strong>Constraint:</strong> The combination of (image_id, index) must be
     * unique,
     * ensuring no duplicate indices exist for a single image.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong> An image with 4 GCPs will have indices: 0, 1, 2, 3
     * </p>
     */
    @Column(nullable = false)
    private int index;

    /**
     * Residual error for this GCP after transformation calculation.
     * 
     * <p>
     * The residual represents the difference between the predicted position
     * (after applying the calculated transformation) and the actual specified
     * position.
     * Lower residuals indicate higher accuracy.
     * </p>
     * 
     * <p>
     * <strong>Calculated by GDAL during georeferencing:</strong>
     * </p>
     * <ul>
     * <li>Residual &lt; 1 pixel: Excellent accuracy</li>
     * <li>Residual 1-3 pixels: Good accuracy</li>
     * <li>Residual &gt; 3 pixels: May indicate incorrect GCP placement</li>
     * </ul>
     * 
     * <p>
     * <strong>Note:</strong> This field is nullable as it's only populated after
     * the georeferencing process completes. Initially null when GCPs are created.
     * </p>
     * 
     * <p>
     * Unit: Typically measured in pixels of the source image.
     * </p>
     */
    private Double residual;
}