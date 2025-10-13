package com.amine.pfe.georef_module.gcp.service.port;

import java.util.List;
import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;

/**
 * Service for computing transformation residuals and accuracy metrics in image
 * georeferencing.
 * 
 * <p>
 * This service validates and analyzes Ground Control Points (GCPs) to assess
 * the quality
 * of polynomial transformations used in georeferencing. It provides methods to
 * check GCP
 * sufficiency and compute residual errors and RMSE.
 * </p>
 * 
 * <p>
 * Supported transformation types:
 * </p>
 * <ul>
 * <li>POLYNOMIALE_1: First-degree polynomial transformation</li>
 * <li>POLYNOMIALE_2: Second-degree polynomial transformation</li>
 * <li>POLYNOMIALE_3: Third-degree polynomial transformation</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see GcpDto
 * @see ResidualsResult
 * @see TransformationType
 */
public interface ResidualsService {

    /**
     * Computes transformation residuals and Root Mean Square Error (RMSE) for the
     * given GCPs.
     * 
     * <p>
     * This method performs the complete residual analysis workflow:
     * </p>
     * <ol>
     * <li>Fits a polynomial transformation of the specified degree to the GCPs</li>
     * <li>Computes predicted map coordinates for each GCP using the fitted
     * transformation</li>
     * <li>Calculates the distance (residual) between actual and predicted
     * positions</li>
     * <li>Computes the overall RMSE as a quality metric</li>
     * </ol>
     * 
     * <p>
     * Distance calculation depends on the coordinate system:
     * </p>
     * <ul>
     * <li><b>SRID 4326 (WGS84)</b>: Uses Haversine formula for geographic
     * coordinates (result in meters)</li>
     * <li><b>Other SRIDs</b>: Uses Euclidean distance for projected coordinates
     * (result in map units)</li>
     * </ul>
     * 
     * <p>
     * <b>RMSE Interpretation:</b>
     * </p>
     * <ul>
     * <li>&lt; 1 pixel/meter: Excellent georeferencing accuracy</li>
     * <li>1-2 pixels/meters: Good accuracy</li>
     * <li>. &gt; 3 pixels/meters: Review GCP placement and quality</li>
     * </ul>
     * 
     * @param gcps list of Ground Control Points containing source (image) and map
     *             coordinates.
     *             Must contain at least the minimum number of points required for
     *             the transformation type.
     * @param type the polynomial transformation type to apply (determines the
     *             degree of the polynomial)
     * @param srid the Spatial Reference System Identifier of the map coordinates
     *             (affects distance calculation)
     * @return ResidualsResult containing a list of individual residuals (one per
     *         GCP) and the overall RMSE
     * @throws IllegalArgumentException if gcps is null or empty
     * @throws IllegalArgumentException if the transformation type is not supported
     * @see #getMinimumPointsRequired(TransformationType)
     * @see #hasEnoughGCPs(List, TransformationType)
     */
    ResidualsResult computeResiduals(List<GcpDto> gcps, TransformationType type, Srid srid);

    /**
     * Returns the minimum number of Ground Control Points required for a given
     * transformation type.
     * 
     * <p>
     * The minimum number equals the number of parameters in the polynomial
     * transformation:
     * </p>
     * <table border="1">
     * <tr>
     * <th>Transformation Type</th>
     * <th>Polynomial Degree</th>
     * <th>Number of Parameters</th>
     * <th>Minimum GCPs</th>
     * </tr>
     * <tr>
     * <td>POLYNOMIALE_1</td>
     * <td>1 (Affine)</td>
     * <td>3</td>
     * <td>3</td>
     * </tr>
     * <tr>
     * <td>POLYNOMIALE_2</td>
     * <td>2</td>
     * <td>6</td>
     * <td>6</td>
     * </tr>
     * <tr>
     * <td>POLYNOMIALE_3</td>
     * <td>3</td>
     * <td>10</td>
     * <td>10</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <b>Note:</b> While these are the mathematical minimums, using more GCPs than
     * the minimum
     * (overdetermined system) generally produces more accurate and robust
     * transformations.
     * </p>
     * 
     * @param transformationType the type of polynomial transformation
     * @return the minimum number of GCPs required for this transformation type
     */
    int getMinimumPointsRequired(TransformationType transformationType);

    /**
     * Validates whether sufficient Ground Control Points are available for the
     * requested transformation.
     * 
     * <p>
     * This is a convenience method that checks if the number of provided GCPs meets
     * or exceeds
     * the minimum required for the specified transformation type. It should be
     * called before
     * attempting to compute residuals to avoid computational errors.
     * </p>
     * 
     * <p>
     * <b>Usage example:</b>
     * </p>
     * 
     * <pre>{@code
     * if (!residualsService.hasEnoughGCPs(gcps, TransformationType.POLYNOMIALE_2)) {
     *     int required = residualsService.getMinimumPointsRequired(TransformationType.POLYNOMIALE_2);
     *     throw new InsufficientGCPsException("Need at least " + required + " GCPs");
     * }
     * ResidualsResult result = residualsService.computeResiduals(gcps, TransformationType.POLYNOMIALE_2, srid);
     * }</pre>
     * 
     * @param gcps the list of Ground Control Points to validate
     * @param type the transformation type to validate against
     * @return true if the number of GCPs is sufficient, false otherwise
     * @see #getMinimumPointsRequired(TransformationType)
     */
    boolean hasEnoughGCPs(List<GcpDto> gcps, TransformationType type);
}