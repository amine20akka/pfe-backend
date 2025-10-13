package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of resampling methods used during image transformation
 * and georeferencing operations.
 *
 * <p>
 * Resampling determines how pixel values are interpolated when an image
 * is transformed — for example, during reprojection, rotation or scaling.
 * Choosing the right resampling method affects the resulting image’s quality,
 * accuracy and processing speed.
 * </p>
 *
 * <p>
 * <strong>Key Considerations:</strong>
 * </p>
 * <ul>
 * <li>Nature of the raster data (continuous vs categorical)</li>
 * <li>Required output quality</li>
 * <li>Processing speed and performance</li>
 * <li>Preservation of radiometric values</li>
 * </ul>
 *
 * <p>
 * <strong>Resampling Method Comparison:</strong>
 * </p>
 * <table border="1">
 * <tr>
 * <th>Method</th>
 * <th>Type</th>
 * <th>Quality</th>
 * <th>Speed</th>
 * <th>Recommended For</th>
 * </tr>
 * <tr>
 * <td>NEAREST</td>
 * <td>Discrete (no interpolation)</td>
 * <td>Low (blocky appearance)</td>
 * <td>Fastest</td>
 * <td>Categorical data (land cover, labels, masks)</td>
 * </tr>
 * <tr>
 * <td>BILINEAR</td>
 * <td>Continuous (linear interpolation)</td>
 * <td>Medium (smoother)</td>
 * <td>Fast</td>
 * <td>Continuous data (elevation, temperature)</td>
 * </tr>
 * <tr>
 * <td>CUBIC</td>
 * <td>Continuous (cubic convolution)</td>
 * <td>High (sharp & smooth results)</td>
 * <td>Slower</td>
 * <td>High-quality visualization, RGB imagery</td>
 * </tr>
 * </table>
 *
 * <p>
 * <strong>JSON Serialization:</strong>
 * </p>
 * <ul>
 * <li>JSON output: e.g. <code>"Bilinear"</code></li>
 * <li>JSON input: accepts case-insensitive labels like <code>"bilinear"</code>
 * or <code>"BILINEAR"</code></li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>
 * // Selecting a resampling method
 * GeorefImage image = GeorefImage.builder()
 *         .resamplingMethod(ResamplingMethod.BILINEAR)
 *         .build();
 *
 * // Default method (if none provided)
 * image.setResamplingMethod(ResamplingMethod.getDefault()); // NEAREST
 *
 * // From JSON
 * ResamplingMethod method = ResamplingMethod.fromLabel("cubic"); // Returns CUBIC
 * </pre>
 *
 * <p>
 * <strong>Integration with GDAL:</strong>
 * </p>
 * <p>
 * The selected resampling method is mapped directly to GDAL options
 * (e.g., <code>-r bilinear</code>, <code>-r cubic</code>) during image warping
 * or reprojection.
 * </p>
 *
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see com.amine.pfe.georef_module.entity.GeorefImage
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ResamplingMethod {

    /**
     * Nearest neighbor resampling.
     *
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>No interpolation — takes the nearest pixel value.</li>
     * <li>Preserves exact categorical values (no averaging).</li>
     * <li>Fastest resampling method.</li>
     * <li>May result in a blocky or jagged appearance.</li>
     * </ul>
     *
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li>Land cover classification maps</li>
     * <li>Label rasters</li>
     * <li>Binary masks</li>
     * <li>Discrete or integer-based datasets</li>
     * </ul>
     *
     * <p>
     * <strong>GDAL Option:</strong> <code>-r nearest</code>
     * </p>
     */
    NEAREST("Nearest"),

    /**
     * Bilinear interpolation.
     *
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Interpolates using the 4 nearest pixel values.</li>
     * <li>Produces smoother transitions than NEAREST.</li>
     * <li>Moderate computational cost.</li>
     * <li>Slight smoothing effect (can reduce noise).</li>
     * </ul>
     *
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li>Continuous datasets such as elevation or temperature</li>
     * <li>RGB or grayscale imagery (when accuracy is not critical)</li>
     * <li>Quick visual rendering</li>
     * </ul>
     *
     * <p>
     * <strong>GDAL Option:</strong> <code>-r bilinear</code>
     * </p>
     */
    BILINEAR("Bilinear"),

    /**
     * Cubic convolution interpolation.
     *
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Uses 16 surrounding pixels (4x4 window).</li>
     * <li>Produces sharper and smoother images than bilinear.</li>
     * <li>Higher computational cost (slower).</li>
     * <li>May introduce small overshoot artifacts near edges.</li>
     * </ul>
     *
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li>High-quality orthophotos or satellite imagery</li>
     * <li>Visualization where aesthetics are important</li>
     * <li>RGB or multispectral images</li>
     * </ul>
     *
     * <p>
     * <strong>GDAL Option:</strong> <code>-r cubic</code>
     * </p>
     */
    CUBIC("Cubic");

    /**
     * Human-readable label for JSON serialization and display.
     */
    private final String label;

    /**
     * Constructor for resampling method.
     *
     * @param label the human-readable label
     */
    ResamplingMethod(String label) {
        this.label = label;
    }

    /**
     * Returns the label used for JSON serialization.
     *
     * <p>
     * <strong>Example:</strong> {@code ResamplingMethod.BILINEAR → "Bilinear"}
     * </p>
     *
     * @return the label
     */
    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Creates a {@link ResamplingMethod} enum from a label (case-insensitive).
     *
     * <p>
     * Used by Jackson for JSON deserialization.
     * </p>
     *
     * <p>
     * <strong>Examples:</strong>
     * </p>
     * 
     * <pre>
     * ResamplingMethod.fromLabel("nearest"); // Returns NEAREST
     * ResamplingMethod.fromLabel("Cubic"); // Returns CUBIC
     * </pre>
     *
     * @param label the label to parse
     * @return the corresponding {@link ResamplingMethod}
     * @throws IllegalArgumentException if the label is not recognized
     */
    @JsonCreator
    public static ResamplingMethod fromLabel(String label) {
        for (ResamplingMethod method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown resampling method: " + label);
    }

    /**
     * Returns the default resampling method.
     *
     * <p>
     * Used when no specific method is defined by the user.
     * </p>
     * <p>
     * Defaults to {@link #NEAREST} for maximum performance
     * and to preserve categorical data integrity.
     * </p>
     *
     * <p>
     * <strong>Usage Example:</strong>
     * </p>
     * 
     * <pre>
     * GeorefImage image = new GeorefImage();
     * if (image.getResamplingMethod() == null) {
     *     image.setResamplingMethod(ResamplingMethod.getDefault());
     * }
     * </pre>
     *
     * @return {@link #NEAREST} as the default resampling method
     */
    public static ResamplingMethod getDefault() {
        return ResamplingMethod.NEAREST;
    }
}
