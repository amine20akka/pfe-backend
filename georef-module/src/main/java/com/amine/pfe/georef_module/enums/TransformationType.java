package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TransformationType {
    @JsonProperty("Polynomiale 1")
    POLYNOMIALE_1("Polynomiale 1"),
    
    @JsonProperty("Polynomiale 2")
    POLYNOMIALE_2("Polynomiale 2"),
    
    @JsonProperty("Polynomiale 3")
    POLYNOMIALE_3("Polynomiale 3");
    
    private final String label;
    
    TransformationType(String label) {
        this.label = label;
    }
    
    @JsonValue
    public String getLabel() {
        return label;
    }
    
    @JsonCreator
    public static TransformationType fromLabel(String label) {
        for (TransformationType value : values()) {
            if (value.label.equalsIgnoreCase(label)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown transformation type: " + label);
    }
    
    public static TransformationType getDefault() {
        return TransformationType.POLYNOMIALE_1;
    }
}