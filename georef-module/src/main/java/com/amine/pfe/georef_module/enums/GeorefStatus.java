package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum GeorefStatus {
    UPLOADED("uploaded"), 
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"), 
    FAILED("failed");

    private final String label;

    GeorefStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static GeorefStatus fromLabel(String label) {
        for (GeorefStatus method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown georef status: " + label);
    }
}
