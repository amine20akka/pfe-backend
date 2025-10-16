package com.amine.pfe.georef_module.gcp.dto;

import java.util.List;

import com.amine.pfe.georef_module.gcp.service.port.ResidualsService;

import lombok.Getter;
import lombok.Setter;

/**
 * Internal result object containing raw residual computation results.
 * 
 * <p>
 * This DTO is used internally by the residuals service to return computational
 * results
 * before they are packaged into a {@link ResidualsResponse} for the API layer.
 * It contains
 * the raw residuals list and RMSE value without business logic concerns like
 * success flags
 * or minimum point requirements.
 * </p>
 * 
 * <p>
 * <b>Layer Separation:</b>
 * </p>
 * <ul>
 * <li><b>ResidualsResult:</b> Service layer output (raw computation
 * results)</li>
 * <li><b>ResidualsResponse:</b> API layer output (includes metadata, success
 * flag)</li>
 * </ul>
 * 
 * <p>
 * <b>Usage Pattern:</b>
 * </p>
 * 
 * <pre>{@code
 * // Service layer computes raw results
 * ResidualsResult result = residualsService.computeResiduals(gcps, type, srid);
 * 
 * // Controller/facade transforms to API response
 * ResidualsResponse response = new ResidualsResponse(
 *         true,
 *         gcpsWithResidualsPopulated,
 *         result.getRmse(),
 *         minPointsRequired);
 * }</pre>
 * 
 * <p>
 * <b>Field Details:</b>
 * </p>
 * <ul>
 * <li><b>residuals:</b> List of individual residual values (one per GCP), same
 * order as input</li>
 * <li><b>rmse:</b> Computed Root Mean Square Error across all GCPs</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see ResidualsResponse
 * @see ResidualsService
 */
@Getter
@Setter
public class ResidualsResult {

    /**
     * List of individual residual errors (distance between actual and predicted
     * position).
     * Each value corresponds to one GCP in the same order as the input.
     */
    private List<Double> residuals;

    /**
     * Root Mean Square Error computed as: RMSE = sqrt(sum(residual²) / n).
     * Represents overall georeferencing accuracy in coordinate system units.
     */
    private double rmse;

    /**
     * Constructs a ResidualsResult with computed residuals and RMSE.
     * 
     * @param residuals list of individual residual values for each GCP
     * @param rmse      the Root Mean Square Error across all GCPs
     */
    public ResidualsResult(List<Double> residuals, double rmse) {
        this.residuals = residuals;
        this.rmse = rmse;
    }
}