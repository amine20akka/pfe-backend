package com.amine.pfe.georef_module.gcp.dto;

import java.util.UUID;

import lombok.*;

/**
 * Data Transfer Object representing a Ground Control Point (GCP).
 * 
 * <p>
 * This DTO transfers GCP data between layers (controller, service, repository)
 * and
 * serves as the JSON representation for REST API requests/responses. It
 * decouples the
 * external API contract from internal JPA entities.
 * </p>
 * 
 * <p>
 * <b>Coordinate Systems:</b>
 * </p>
 * <ul>
 * <li><b>Source (sourceX, sourceY):</b> Pixel coordinates in the unreferenced
 * image</li>
 * <li><b>Map (mapX, mapY):</b> Real-world coordinates in the target spatial
 * reference system</li>
 * </ul>
 * 
 * <p>
 * <b>Lombok Annotations:</b>
 * </p>
 * <ul>
 * <li>{@code @Getter/@Setter}: Auto-generates getters/setters for all
 * fields</li>
 * <li>{@code @NoArgsConstructor}: Creates default constructor (required for
 * JSON deserialization)</li>
 * <li>{@code @AllArgsConstructor}: Creates constructor with all fields</li>
 * <li>{@code @Builder}: Enables fluent builder pattern for object creation</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GcpDto {

    /** Unique identifier (UUID) for the GCP. Null for new GCPs. */
    private UUID id;

    /** Reference to the parent georeferenced image. */
    private UUID imageId;

    /** X-coordinate (column) in source image pixel space. */
    private Double sourceX;

    /** Y-coordinate (row) in source image pixel space. */
    private Double sourceY;

    /** X-coordinate in map coordinate system (Easting/Longitude). */
    private Double mapX;

    /** Y-coordinate in map coordinate system (Northing/Latitude). */
    private Double mapY;

    /**
     * Sequential index for ordering GCPs within an image. Must be unique per image.
     */
    private int index;

    /**
     * Computed residual error (distance between actual and predicted position).
     * Null until computed.
     */
    private Double residual;

    /**
     * Convenience constructor for creating GCPs with only coordinate data.
     * 
     * <p>
     * Useful for quick GCP creation without ID, imageId, index, or residual.
     * These fields can be set later via setters or during mapping.
     * </p>
     * 
     * @param sourceX X-coordinate in source image
     * @param sourceY Y-coordinate in source image
     * @param mapX    X-coordinate in map system
     * @param mapY    Y-coordinate in map system
     */
    public GcpDto(Double sourceX, Double sourceY, Double mapX, Double mapY) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.mapX = mapX;
        this.mapY = mapY;
    }
}
