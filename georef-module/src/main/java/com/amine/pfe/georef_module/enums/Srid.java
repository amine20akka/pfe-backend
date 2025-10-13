package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum {@code Srid} defines the supported Spatial Reference System Identifiers
 * (SRIDs)
 * used in the Georeferencing module of the WebGIS project.
 *
 * <p>
 * SRIDs identify the coordinate reference system used for geospatial data.
 * This allows layers and geometries to be correctly located on the Earth's
 * surface.
 *
 * <p>
 * Currently, the system supports:
 * <ul>
 * <li>{@link #_4326} — WGS84, a geographic coordinate system based on latitude
 * and longitude.</li>
 * <li>{@link #_3857} — Web Mercator, a projection commonly used by most web
 * mapping applications (e.g. OpenStreetMap, Google Maps).</li>
 * </ul>
 *
 * <p>
 * This enum is serialized and deserialized as a number in JSON format.
 * Example JSON representation:
 * 
 * <pre>
 * {
 *   "srid": 3857
 * }
 * </pre>
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Srid srid = Srid.fromCode(4326);
 * System.out.println(srid); // _4326
 * }</pre>
 *
 * @author Amine
 * @since 1.0
 */
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum Srid {

    /** WGS84 — Geographic coordinate system (latitude/longitude). */
    @JsonProperty("4326")
    _4326(4326),

    /**
     * Web Mercator — Common projection for web maps (used by OpenLayers, Google
     * Maps, etc.).
     */
    @JsonProperty("3857")
    _3857(3857);

    private final int code;

    /**
     * Constructs a new {@code Srid} enum constant with the specified SRID code.
     *
     * @param code the spatial reference identifier (SRID) code associated with this enum constant
     */
    Srid(int code) {
        this.code = code;
    }

    /**
     * Returns the integer SRID code (e.g., 4326 or 3857).
     *
     * @return the SRID code
     */
    @JsonValue
    public int getCode() {
        return code;
    }

    /**
     * Converts an integer value to the corresponding {@link Srid} enum constant.
     *
     * @param code the integer SRID value
     * @return the matching {@link Srid}
     * @throws IllegalArgumentException if the SRID code is unknown
     */
    @JsonCreator
    public static Srid fromCode(int code) {
        for (Srid s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown SRID: " + code);
    }

    /**
     * Returns the default SRID used by the system.
     * By convention, Web Mercator (EPSG:3857) is used for most web-based maps.
     *
     * @return the default {@link Srid}
     */
    public static Srid getDefault() {
        return Srid._3857;
    }
}
