package com.amine.pfe.georef_module.gcp.service.port;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;

/**
 * Factory interface for creating Ground Control Point (GCP) entities.
 * 
 * <p>
 * This factory provides a clean abstraction for GCP instantiation,
 * encapsulating
 * the creation logic and ensuring consistent object initialization. It follows
 * the
 * Factory design pattern to decouple GCP creation from business logic.
 * </p>
 * 
 * <p>
 * <b>Purpose:</b>
 * </p>
 * <ul>
 * <li>Centralize GCP object creation logic</li>
 * <li>Ensure all required fields are properly initialized</li>
 * <li>Maintain consistency across the application when creating GCPs</li>
 * <li>Facilitate unit testing through dependency injection</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see Gcp
 * @see GeorefImage
 */
public interface GcpFactory {

    /**
     * Creates a new Ground Control Point (GCP) entity with the specified
     * parameters.
     * 
     * <p>
     * This method constructs a fully initialized GCP that links a point in the
     * source image
     * to its corresponding location in the map coordinate system. The created GCP
     * can be used
     * for polynomial transformation fitting and accuracy assessment.
     * </p>
     * 
     * <p>
     * <b>Parameter Details:</b>
     * </p>
     * <ul>
     * <li><b>image:</b> The georeferenced image this GCP belongs to (establishes
     * the relationship)</li>
     * <li><b>sourceX, sourceY:</b> Pixel coordinates in the source image (typically
     * non-negative)</li>
     * <li><b>mapX, mapY:</b> Coordinates in the target spatial reference system
     * (e.g., UTM, WGS84)</li>
     * <li><b>index:</b> Sequential identifier for the GCP within the image (for
     * ordering and reference)</li>
     * </ul>
     * 
     * <p>
     * <b>Usage Example:</b>
     * </p>
     * 
     * <pre>{@code
     * // Create a GCP for a building corner
     * Gcp gcp = gcpFactory.createGcp(
     *         georefImage, // The image being georeferenced
     *         245.5, // X pixel coordinate in image
     *         189.3, // Y pixel coordinate in image
     *         567832.45, // X coordinate in UTM zone 32N
     *         4123456.78, // Y coordinate in UTM zone 32N
     *         1 // First GCP (index 1)
     * );
     * }</pre>
     * 
     * <p>
     * <b>Best Practices:</b>
     * </p>
     * <ul>
     * <li>Use at least 3 GCPs for affine transformation (POLYNOMIALE_1)</li>
     * <li>Distribute GCPs evenly across the image for better accuracy</li>
     * <li>Choose distinctive features (building corners, road intersections) for
     * precise placement</li>
     * <li>Maintain consistent coordinate system across all GCPs for an image</li>
     * </ul>
     * 
     * @param image   the georeferenced image entity this GCP belongs to (must not
     *                be null)
     * @param sourceX the X-coordinate (column) in the source image pixel space
     * @param sourceY the Y-coordinate (row) in the source image pixel space
     * @param mapX    the X-coordinate in the target map coordinate system (e.g.,
     *                Easting in UTM, Longitude in WGS84)
     * @param mapY    the Y-coordinate in the target map coordinate system (e.g.,
     *                Northing in UTM, Latitude in WGS84)
     * @param index   the sequential index/identifier for this GCP (typically
     *                starting from 1)
     * @return a fully initialized GCP entity ready to be persisted or used in
     *         calculations
     * @throws NullPointerException if image is null (implementation-dependent)
     */
    Gcp createGcp(GeorefImage image, Double sourceX, Double sourceY, double mapX, double mapY, Integer index);
}