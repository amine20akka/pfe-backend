package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum {@code TransformationType} defines the polynomial transformation
 * types used during georeferencing of raster images.
 *
 * <p>
 * Polynomial transformations are mathematical functions that map source image
 * coordinates to target map coordinates. The degree of the polynomial
 * determines
 * the complexity and accuracy of the transformation:
 * </p>
 *
 * <ul>
 * <li>1st-degree polynomial: affine transformation, simple scaling, rotation,
 * translation.</li>
 * <li>2nd-degree polynomial: allows more complex distortions, suitable for
 * moderate image deformation.</li>
 * <li>3rd-degree polynomial: captures higher-order distortions, best for highly
 * warped images.</li>
 * </ul>
 *
 * <p>
 * <strong>JSON Serialization:</strong>
 * </p>
 * <ul>
 * <li>JSON output: e.g., <code>"Polynomiale 1"</code></li>
 * <li>JSON input: accepts case-insensitive values, e.g.,
 * <code>"polynomiale 2"</code></li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>
 * // Selecting transformation type
 * GeorefImage image = GeorefImage.builder()
 *         .transformationType(TransformationType.POLYNOMIALE_2)
 *         .build();
 *
 * // Default transformation (if none specified)
 * image.setTransformationType(TransformationType.getDefault()); // POLYNOMIALE_1
 *
 * // From JSON
 * TransformationType type = TransformationType.fromLabel("Polynomiale 3"); // Returns POLYNOMIALE_3
 * </pre>
 *
 * <p>
 * <strong>Integration with GDAL:</strong>
 * </p>
 * <p>
 * These transformation types are passed to GDAL's <code>gdal_translate</code>
 * or <code>gdalwarp</code> commands when applying polynomial georeferencing.
 * </p>
 *
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see com.amine.pfe.georef_module.entity.GeorefImage
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TransformationType {

    /** First-degree polynomial (affine transformation). */
    @JsonProperty("Polynomiale 1")
    POLYNOMIALE_1("Polynomiale 1"),

    /** Second-degree polynomial, suitable for moderate distortions. */
    @JsonProperty("Polynomiale 2")
    POLYNOMIALE_2("Polynomiale 2"),

    /** Third-degree polynomial, suitable for high-order distortions. */
    @JsonProperty("Polynomiale 3")
    POLYNOMIALE_3("Polynomiale 3");

    private final String label;

    TransformationType(String label) {
        this.label = label;
    }

    /**
     * Returns the label for JSON serialization.
     *
     * <p>
     * Example: {@code TransformationType.POLYNOMIALE_2 → "Polynomiale 2"}
     * </p>
     *
     * @return the label
     */
    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Converts a JSON label into the corresponding {@link TransformationType}.
     *
     * <p>
     * Case-insensitive.
     * </p>
     *
     * <p>
     * Example:
     * </p>
     * 
     * <pre>
     * TransformationType.fromLabel("Polynomiale 1"); // Returns POLYNOMIALE_1
     * </pre>
     *
     * @param label the label from JSON
     * @return the corresponding {@link TransformationType}
     * @throws IllegalArgumentException if the label is unknown
     */
    @JsonCreator
    public static TransformationType fromLabel(String label) {
        for (TransformationType value : values()) {
            if (value.label.equalsIgnoreCase(label)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown transformation type: " + label);
    }

    /**
     * Returns the default transformation type.
     *
     * <p>
     * By convention, the first-degree polynomial (affine) is used as the default
     * for most standard georeferencing tasks.
     * </p>
     *
     * <p>
     * Usage Example:
     * </p>
     * 
     * <pre>
     * GeorefImage image = new GeorefImage();
     * if (image.getTransformationType() == null) {
     *     image.setTransformationType(TransformationType.getDefault());
     * }
     * </pre>
     *
     * @return {@link #POLYNOMIALE_1} as the default transformation type
     */
    public static TransformationType getDefault() {
        return TransformationType.POLYNOMIALE_1;
    }
}
