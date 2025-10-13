package com.amine.pfe.georef_module.entity;

import java.util.UUID;

import com.amine.pfe.georef_module.enums.LayerStatus;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a published georeferenced raster layer.
 * 
 * <p>
 * A GeorefLayer is the result of the georeferencing process, where a raster image
 * is transformed and published as a map layer via a map server (e.g., GeoServer).
 * This entity stores metadata required to access and manage the published layer.
 * </p>
 * 
 * <p>
 * <strong>Layer Publication Process:</strong>
 * </p>
 * <ol>
 * <li>User uploads and georeferences a raster image</li>
 * <li>The image is published to a map server workspace and store</li>
 * <li>A layer is created and exposed via WMS (Web Map Service)</li>
 * <li>This entity records the workspace, store, layer name, WMS URL, and status</li>
 * </ol>
 * 
 * <p>
 * <strong>Database Schema:</strong>
 * </p>
 * <ul>
 * <li>Table: <code>georef_layers</code></li>
 * <li>Schema: <code>georef</code></li>
 * <li>Unique Constraint: Each image can have only one published layer</li>
 * </ul>
 * 
 * <p>
 * <strong>Relationships:</strong>
 * </p>
 * <ul>
 * <li>One-to-one with {@link GeorefImage}: Each layer is linked to a single georeferenced image</li>
 * <li>Layer status managed via {@link LayerStatus} enum</li>
 * </ul>
 * 
 * <p>
 * <strong>Example Usage:</strong>
 * </p>
 * 
 * <pre>
 * GeorefLayer layer = GeorefLayer.builder()
 *         .image(georefImage)
 *         .workspace("default")
 *         .storeName("raster_store")
 *         .layerName("tunis_satellite")
 *         .wmsUrl("http://localhost:8080/geoserver/wms")
 *         .status(LayerStatus.PUBLISHED)
 *         .build();
 * </pre>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see GeorefImage
 * @see LayerStatus
 */
@Entity
@Table(name = "georef_layers", schema = "georef")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeorefLayer {

    /**
     * Unique identifier for the georeferenced layer.
     * <p>
     * Generated automatically using UUID strategy for global uniqueness.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * The georeferenced image associated with this layer.
     * <p>
     * One-to-one relationship; each layer is linked to a single image.
     * </p>
     * <p>
     * Database: Foreign key to the <code>georef_images</code> table.
     * </p>
     */
    @OneToOne
    @JoinColumn(name = "image_id", nullable = false, unique = true)
    private GeorefImage image;

    /**
     * Workspace name in the map server where the layer is published.
     * <p>
     * Used to organize layers in the map server (e.g., GeoServer).
     * </p>
     */
    @Column(nullable = false)
    private String workspace;

    /**
     * Store name in the map server where the raster data is stored.
     * <p>
     * Identifies the data store containing the raster source.
     * </p>
     */
    @Column(nullable = false)
    private String storeName;

    /**
     * Name of the published layer.
     * <p>
     * Used to reference the layer in WMS requests and map server management.
     * </p>
     */
    @Column(nullable = false)
    private String layerName;

    /**
     * WMS (Web Map Service) URL for accessing the published layer.
     * <p>
     * Provides the endpoint for map clients to retrieve the layer.
     * </p>
     */
    @Column(nullable = false)
    private String wmsUrl;

    /**
     * Current status of the layer publication.
     * <p>
     * Managed via {@link LayerStatus} enum (e.g., PUBLISHED, FAILED, PENDING).
     * </p>
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LayerStatus status;
}
