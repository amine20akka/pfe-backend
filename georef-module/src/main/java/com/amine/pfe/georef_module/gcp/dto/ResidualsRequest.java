package com.amine.pfe.georef_module.gcp.dto;

import java.util.UUID;

import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;

import lombok.Data;

/**
 * Request DTO for computing transformation residuals and RMSE for GCPs.
 * 
 * <p>
 * This DTO encapsulates the parameters needed to perform residual analysis on
 * an image's
 * Ground Control Points. It specifies which transformation to apply and which
 * coordinate
 * system to use for distance calculations.
 * </p>
 * 
 * <p>
 * <b>Typical REST Endpoint:</b>
 * </p>
 * 
 * <pre>{@code
 * POST /api/georef/images/{imageId}/gcps/residuals
 * {
 *   "imageId": "a3f1b2c4-...",
 *   "type": "POLYNOMIALE_2",
 *   "srid": "_4326"
 * }
 * }</pre>
 * 
 * <p>
 * <b>Validation Requirements:</b>
 * </p>
 * <ul>
 * <li>Image must exist and have sufficient GCPs for the transformation
 * type</li>
 * <li>SRID should match the coordinate system of the map coordinates</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see ResidualsResponse
 * @see TransformationType
 * @see Srid
 */
@Data
public class ResidualsRequest {

    /** ID of the georeferenced image whose GCPs should be analyzed. */
    private UUID imageId;

    /**
     * Polynomial transformation type to apply.
     * <ul>
     * <li>POLYNOMIALE_1: Affine (requires 3+ GCPs)</li>
     * <li>POLYNOMIALE_2: 2nd degree (requires 6+ GCPs)</li>
     * <li>POLYNOMIALE_3: 3rd degree (requires 10+ GCPs)</li>
     * </ul>
     */
    private TransformationType type;

    /**
     * Spatial Reference System Identifier for distance calculation.
     * <ul>
     * <li>_4326 (WGS84): Uses Haversine formula for geographic coordinates</li>
     * <li>_3857 (Web Mercator): Uses Euclidean distance for projected coordinates</li>
     * </ul>
     */
    private Srid srid;
}