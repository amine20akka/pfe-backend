package com.amine.pfe.georef_module.gcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amine.pfe.georef_module.enums.Srid;
import com.amine.pfe.georef_module.enums.TransformationType;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.gcp.dto.ResidualsResult;
import com.amine.pfe.georef_module.gcp.service.impl.ResidualsServiceImpl;

public class ResidualServiceTest {
        private ResidualsServiceImpl residualsService;

    @BeforeEach
    void setUp() {
        residualsService = new ResidualsServiceImpl();
    }

    // ---------------------------
    // Tests de getMinimumPointsRequired
    // ---------------------------

    @Test
    void testGetMinimumPointsRequired_Polynomiale1() {
        int minPoints = residualsService.getMinimumPointsRequired(TransformationType.POLYNOMIALE_1);
        assertEquals(3, minPoints);
    }

    @Test
    void testGetMinimumPointsRequired_Polynomiale2() {
        int minPoints = residualsService.getMinimumPointsRequired(TransformationType.POLYNOMIALE_2);
        assertEquals(6, minPoints);
    }

    @Test
    void testGetMinimumPointsRequired_Polynomiale3() {
        int minPoints = residualsService.getMinimumPointsRequired(TransformationType.POLYNOMIALE_3);
        assertEquals(10, minPoints);
    }

    // ---------------------------
    // Tests de hasEnoughGCPs
    // ---------------------------

    @Test
    void testHasEnoughGCPs_EnoughPoints() {
        List<GcpDto> gcps = createDummyGcps(5);
        assertTrue(residualsService.hasEnoughGCPs(gcps, TransformationType.POLYNOMIALE_1));
    }

    @Test
    void testHasEnoughGCPs_NotEnoughPoints() {
        List<GcpDto> gcps = createDummyGcps(2);
        assertFalse(residualsService.hasEnoughGCPs(gcps, TransformationType.POLYNOMIALE_1));
    }

    @Test
    void testHasEnoughGCPs_ExactlyEnoughPoints() {
        List<GcpDto> gcps = createDummyGcps(6);
        assertTrue(residualsService.hasEnoughGCPs(gcps, TransformationType.POLYNOMIALE_2));
    }

    // ---------------------------
    // Tests de computeResiduals
    // ---------------------------

    @Test
    void testComputeResiduals_Polynomiale1_Euclidean() {
        List<GcpDto> gcps = createDummyGcps(5);
        ResidualsResult result = residualsService.computeResiduals(gcps, TransformationType.POLYNOMIALE_1, Srid._3857);

        assertNotNull(result);
        assertEquals(5, result.getResiduals().size());
        assertTrue(result.getRmse() >= 0);
    }

    @Test
    void testComputeResiduals_Polynomiale2_Euclidean() {
        List<GcpDto> gcps = createDummyGcps(6);
        ResidualsResult result = residualsService.computeResiduals(gcps, TransformationType.POLYNOMIALE_2, Srid._3857);

        assertNotNull(result);
        assertEquals(6, result.getResiduals().size());
    }

    @Test
    void testComputeResiduals_Polynomiale3_Haversine() {
        List<GcpDto> gcps = createDummyGcps(10);
        ResidualsResult result = residualsService.computeResiduals(gcps, TransformationType.POLYNOMIALE_3, Srid._4326);

        assertNotNull(result);
        assertEquals(10, result.getResiduals().size());
    }

    // ---------------------------
    // Cas particulier : vérifier comportement pour mauvaise donnée
    // ---------------------------

    @Test
    void testComputeResiduals_WithMinimalPoints() {
        List<GcpDto> gcps = createDummyGcps(3);
        ResidualsResult result = residualsService.computeResiduals(gcps, TransformationType.POLYNOMIALE_1, Srid._4326);

        assertNotNull(result);
        assertEquals(3, result.getResiduals().size());
    }

    @Test
    void testComputeResiduals_ThrowsIllegalArgumentException_OnInvalidDegree() {
        ResidualsServiceImpl service = new ResidualsServiceImpl() {
            @Override
            public ResidualsResult computeResiduals(List<GcpDto> gcps, TransformationType type, Srid srid) {
                return super.computeResiduals(gcps, null, srid);
            }
        };
        List<GcpDto> gcps = createDummyGcps(3);

        assertThrows(NullPointerException.class, () -> service.computeResiduals(gcps, null, Srid._3857));
    }

    // ---------------------------
    // Helpers
    // ---------------------------

    private List<GcpDto> createDummyGcps(int count) {
        List<GcpDto> gcps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            gcps.add(new GcpDto(
                    100.44 + i,    // sourceX
                    50.44 + i,     // sourceY
                    100.5 + i,    // mapX
                    50.5 + i      // mapY
            ));
        }
        return gcps;
    }
}
