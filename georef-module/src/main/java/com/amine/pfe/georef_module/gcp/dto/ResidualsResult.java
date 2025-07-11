package com.amine.pfe.georef_module.gcp.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResidualsResult {
    private List<Double> residuals;
    private double rmse;

    public ResidualsResult(List<Double> residuals, double rmse) {
        this.residuals = residuals;
        this.rmse = rmse;
    }
}
