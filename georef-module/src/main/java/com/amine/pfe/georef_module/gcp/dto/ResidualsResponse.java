package com.amine.pfe.georef_module.gcp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO containing computed residuals and RMSE for GCPs.
 * 
 * <p>
 * This DTO returns the results of residual analysis, including individual
 * residuals
 * for each GCP, the overall RMSE quality metric, and updated GCP data with
 * residuals
 * populated. It also includes validation information about minimum points
 * required.
 * </p>
 * 
 * <p>
 * <b>Response Structure:</b>
 * </p>
 * <ul>
 * <li><b>success:</b> Whether computation succeeded (sufficient GCPs, valid
 * parameters)</li>
 * <li><b>gcpDtos:</b> GCPs with their computed residuals populated</li>
 * <li><b>rmse:</b> Root Mean Square Error (lower is better)</li>
 * <li><b>minPointsRequired:</b> Minimum GCPs needed for this transformation
 * type</li>
 * </ul>
 * 
 * <p>
 * <b>RMSE Interpretation:</b>
 * </p>
 * <ul>
 * <li>&lt; 1 meter/pixel: Excellent georeferencing accuracy</li>
 * <li>1-2 meters/pixels: Good accuracy</li>
 * <li>.&gt; 3 meters/pixels: Review GCP placement</li>
 * </ul>
 * 
 * <p>
 * <b>Example JSON Response:</b>
 * </p>
 * 
 * <pre>{@code
 * {
 *   "success": true,
 *   "gcpDtos": [
 *     {"id": "...", "sourceX": 100.5, "sourceY": 200.3, "mapX": 567832.45, 
 *      "mapY": 4123456.78, "index": 1, "residual": 0.85},
 *     {"id": "...", "sourceX": 450.2, "sourceY": 350.8, "mapX": 568012.33, 
 *      "mapY": 4123612.90, "index": 2, "residual": 1.23}
 *   ],
 *   "rmse": 1.04,
 *   "minPointsRequired": 6
 * }
 * }</pre>
 * 
 * <p>
 * <b>Error Case (Insufficient GCPs):</b>
 * </p>
 * 
 * <pre>{@code
 * {
 *   "success": false,
 *   "gcpDtos": [...],  // Current GCPs without residuals
 *   "rmse": null,
 *   "minPointsRequired": 6
 * }
 * }</pre>
 * 
 * @author Amine
 * @version 1.0
 * @see ResidualsRequest
 * @see GcpDto
 */
@Data
@AllArgsConstructor
public class ResidualsResponse {

    /**
     * Indicates whether residual computation succeeded.
     * False if insufficient GCPs or invalid parameters.
     */
    private boolean success;

    /**
     * List of GCPs with computed residuals populated in each GcpDto.
     * Order matches the input GCP order.
     */
    private List<GcpDto> gcpDtos;

    /**
     * Root Mean Square Error (RMSE) in coordinate system units (meters for
     * geographic).
     * Null if success is false.
     */
    private Double rmse;

    /**
     * Minimum number of GCPs required for the requested transformation type.
     * Useful for client-side validation and error messaging.
     */
    private int minPointsRequired;
}