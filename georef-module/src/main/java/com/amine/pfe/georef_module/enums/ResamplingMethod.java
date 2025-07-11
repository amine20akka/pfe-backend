package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ResamplingMethod {
    NEAREST("Nearest"),
    BILINEAR("Bilinear"),
    CUBIC("Cubic");

    private final String label;

    ResamplingMethod(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ResamplingMethod fromLabel(String label) {
        for (ResamplingMethod method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown resampling method: " + label);
    }

    public static ResamplingMethod getDefault() {
        return ResamplingMethod.NEAREST;
    }
}

