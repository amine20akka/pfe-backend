package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum LayerStatus {
    PENDING("pending"), 
    PUBLISHED("published"), 
    FAILED("failed");

    private final String label;

    LayerStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static LayerStatus fromLabel(String label) {
        for (LayerStatus method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown georef status: " + label);
    }
}

