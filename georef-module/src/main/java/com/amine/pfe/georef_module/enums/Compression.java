package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Compression {
    NONE("None"),
    LZW("LZW"),
    JPEG("JPEG"),
    DEFLATE("Deflate");

    private final String label;

    Compression(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Compression fromLabel(String label) {
        for (Compression c : values()) {
            if (c.label.equalsIgnoreCase(label)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown compression: " + label);
    }

    public static Compression getDefault() {
        return Compression.NONE;
    }
}

