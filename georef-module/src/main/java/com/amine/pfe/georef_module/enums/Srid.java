package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.NUMBER) 
public enum Srid {
    @JsonProperty("4326")
    _4326(4326),
    
    @JsonProperty("3857")
    _3857(3857);
    
    private final int code;
    
    Srid(int code) {
        this.code = code;
    }
    
    @JsonValue
    public int getCode() {
        return code;
    }
    
    @JsonCreator
    public static Srid fromCode(int code) {
        for (Srid s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown SRID: " + code);
    }
    
    public static Srid getDefault() {
        return Srid._3857;
    }
}

