package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum {@code LayerStatus} represents the publication state of a map layer
 * within the Georeferencing module of the WebGIS project.
 *
 * <p>
 * Each layer can be in one of the following states:
 * <ul>
 * <li>{@link #PENDING} – Waiting for processing or publication.</li>
 * <li>{@link #PUBLISHED} – Successfully published to the GeoServer.</li>
 * <li>{@link #FAILED} – The publication or processing operation failed.</li>
 * </ul>
 *
 * <p>
 * This enumeration is serialized and deserialized as a string using Jackson,
 * thanks to the {@link JsonValue} and {@link JsonCreator} annotations.
 * The JSON representation uses lowercase labels, for example:
 * 
 * <pre>
 * {
 *   "status": "published"
 * }
 * </pre>
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * LayerStatus status = LayerStatus.fromLabel("pending");
 * System.out.println(status); // PENDING
 * }</pre>
 *
 * @author Amine
 * @since 1.0
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum LayerStatus {

    /** The layer is waiting for validation or publication. */
    PENDING("pending"),

    /** The layer has been successfully published to the GeoServer. */
    PUBLISHED("published"),

    /** The publication or processing operation has failed. */
    FAILED("failed");

    private final String label;

    LayerStatus(String label) {
        this.label = label;
    }

    /**
     * Returns the lowercase label used in JSON serialization.
     *
     * @return the label (e.g., "pending")
     */
    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Converts a JSON string into a {@link LayerStatus} value.
     *
     * @param label the status value received from JSON
     * @return the matching {@link LayerStatus}
     * @throws IllegalArgumentException if the label is unknown
     */
    @JsonCreator
    public static LayerStatus fromLabel(String label) {
        for (LayerStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown georef status: " + label);
    }
}
