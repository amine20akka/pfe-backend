package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of compression methods for georeferenced GeoTIFF output files.
 * 
 * <p>
 * Compression reduces the file size of the georeferenced images while
 * maintaining
 * varying levels of quality. The choice of compression method depends on:
 * </p>
 * <ul>
 * <li>Image type (aerial photos, scanned maps, satellite imagery)</li>
 * <li>Quality requirements (lossless vs lossy)</li>
 * <li>File size constraints</li>
 * <li>Processing speed requirements</li>
 * <li>Compatibility with GIS software</li>
 * </ul>
 * 
 * <p>
 * <strong>Compression Comparison:</strong>
 * </p>
 * <table border="1">
 * <tr>
 * <th>Method</th>
 * <th>Type</th>
 * <th>Compression Ratio</th>
 * <th>Speed</th>
 * <th>Best For</th>
 * </tr>
 * <tr>
 * <td>NONE</td>
 * <td>-</td>
 * <td>1:1 (no compression)</td>
 * <td>Fastest</td>
 * <td>Testing, temporary files</td>
 * </tr>
 * <tr>
 * <td>LZW</td>
 * <td>Lossless</td>
 * <td>2:1 to 4:1</td>
 * <td>Fast</td>
 * <td>General purpose (recommended)</td>
 * </tr>
 * <tr>
 * <td>DEFLATE</td>
 * <td>Lossless</td>
 * <td>3:1 to 5:1</td>
 * <td>Medium</td>
 * <td>Maximum lossless compression</td>
 * </tr>
 * <tr>
 * <td>JPEG</td>
 * <td>Lossy</td>
 * <td>10:1 to 50:1</td>
 * <td>Fast</td>
 * <td>Aerial photos, RGB imagery</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <strong>JSON Serialization:</strong>
 * </p>
 * <p>
 * This enum uses custom Jackson annotations for JSON handling:
 * </p>
 * <ul>
 * <li>JSON output: <code>"LZW"</code> (using label)</li>
 * <li>JSON input: Accepts both "LZW" and "lzw" (case-insensitive)</li>
 * </ul>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>
 * // Setting compression for georeferencing
 * GeorefImage image = GeorefImage.builder()
 *         .compression(Compression.LZW) // Recommended default
 *         .build();
 * 
 * // Or use default
 * image.setCompression(Compression.getDefault()); // Returns NONE
 * 
 * // From JSON
 * Compression comp = Compression.fromLabel("deflate"); // Case-insensitive
 * </pre>
 * 
 * <p>
 * <strong>Integration with GDAL:</strong>
 * </p>
 * <p>
 * These compression values are passed directly to GDAL's gdal_translate or
 * gdalwarp
 * commands using the <code>-co COMPRESS=LZW</code> creation option.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see com.amine.pfe.georef_module.entity.GeorefImage
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Compression {

    /**
     * No compression applied to the output GeoTIFF.
     * 
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>File Size: Largest (original size)</li>
     * <li>Quality: Perfect (no degradation)</li>
     * <li>Processing Speed: Fastest (no compression overhead)</li>
     * <li>Compatibility: Universal</li>
     * </ul>
     * 
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li>Temporary processing files</li>
     * <li>Testing and development</li>
     * <li>When disk space is not a concern</li>
     * <li>Maximum processing speed required</li>
     * </ul>
     * 
     * <p>
     * <strong>GDAL Option:</strong> <code>-co COMPRESS=NONE</code>
     * </p>
     */
    NONE("None"),

    /**
     * LZW (Lempel-Ziv-Welch) lossless compression.
     * 
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>File Size: 50-25% of original (2:1 to 4:1 compression)</li>
     * <li>Quality: Perfect (lossless, no degradation)</li>
     * <li>Processing Speed: Fast</li>
     * <li>Compatibility: Excellent (widely supported)</li>
     * </ul>
     * 
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li><strong>Recommended default for most scenarios</strong></li>
     * <li>Scanned historical maps</li>
     * <li>Topographic maps</li>
     * <li>Any imagery where quality must be preserved</li>
     * <li>When GIS analysis requires exact pixel values</li>
     * </ul>
     * 
     * <p>
     * <strong>Advantages:</strong>
     * </p>
     * <ul>
     * <li>Good balance between file size and speed</li>
     * <li>No quality loss</li>
     * <li>Patent-free (since 2004)</li>
     * <li>Supported by all major GIS software</li>
     * </ul>
     * 
     * <p>
     * <strong>GDAL Option:</strong> <code>-co COMPRESS=LZW</code>
     * </p>
     */
    LZW("LZW"),

    /**
     * JPEG lossy compression for RGB/grayscale imagery.
     * 
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>File Size: 10-2% of original (10:1 to 50:1 compression)</li>
     * <li>Quality: Lossy (some degradation, quality configurable)</li>
     * <li>Processing Speed: Fast</li>
     * <li>Compatibility: Good (standard JPEG format)</li>
     * </ul>
     * 
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li>Aerial photographs (orthophotos)</li>
     * <li>Satellite imagery (RGB natural color)</li>
     * <li>Large RGB raster datasets where file size is critical</li>
     * <li>Visual display purposes (not for analysis)</li>
     * </ul>
     * 
     * <p>
     * <strong>Advantages:</strong>
     * </p>
     * <ul>
     * <li>Smallest file sizes possible</li>
     * <li>Fast compression and decompression</li>
     * <li>Acceptable quality for visual use</li>
     * </ul>
     * 
     * <p>
     * <strong>Limitations:</strong>
     * </p>
     * <ul>
     * <li>NOT suitable for images with alpha channel (transparency)</li>
     * <li>NOT suitable for classification maps or categorical data</li>
     * <li>NOT suitable for single-band or multi-spectral imagery</li>
     * <li>Quality loss may affect GIS analysis accuracy</li>
     * <li>Creates compression artifacts around sharp edges</li>
     * </ul>
     * 
     * <p>
     * <strong>GDAL Option:</strong>
     * <code>-co COMPRESS=JPEG -co JPEG_QUALITY=85</code>
     * </p>
     * 
     * <p>
     * <strong>Warning:</strong> Use only for RGB aerial photos or similar visual
     * imagery!
     * </p>
     */
    JPEG("JPEG"),

    /**
     * DEFLATE (ZIP) lossless compression.
     * 
     * <p>
     * <strong>Characteristics:</strong>
     * </p>
     * <ul>
     * <li>File Size: 33-20% of original (3:1 to 5:1 compression)</li>
     * <li>Quality: Perfect (lossless, no degradation)</li>
     * <li>Processing Speed: Medium (slower than LZW)</li>
     * <li>Compatibility: Excellent (widely supported)</li>
     * </ul>
     * 
     * <p>
     * <strong>Use Cases:</strong>
     * </p>
     * <ul>
     * <li>When maximum lossless compression is needed</li>
     * <li>Storage-constrained environments</li>
     * <li>High-resolution imagery archives</li>
     * <li>When processing time is less critical than file size</li>
     * </ul>
     * 
     * <p>
     * <strong>Advantages:</strong>
     * </p>
     * <ul>
     * <li>Better compression ratio than LZW</li>
     * <li>No quality loss</li>
     * <li>Open standard (no licensing issues)</li>
     * <li>Very good for imagery with large uniform areas</li>
     * </ul>
     * 
     * <p>
     * <strong>Comparison with LZW:</strong>
     * </p>
     * <ul>
     * <li>DEFLATE: Smaller files, slower processing</li>
     * <li>LZW: Larger files, faster processing</li>
     * <li>Difference typically 10-30% in file size</li>
     * </ul>
     * 
     * <p>
     * <strong>GDAL Option:</strong> <code>-co COMPRESS=DEFLATE -co ZLEVEL=6</code>
     * </p>
     */
    DEFLATE("Deflate");

    /**
     * Human-readable label for the compression method.
     * 
     * <p>
     * Used for JSON serialization/deserialization and display purposes.
     * Matches the GDAL compression option names for consistency.
     * </p>
     */
    private final String label;

    /**
     * Constructor for Compression enum values.
     * 
     * @param label the human-readable label for this compression method
     */
    Compression(String label) {
        this.label = label;
    }

    /**
     * Returns the label for JSON serialization.
     * 
     * <p>
     * When this enum is serialized to JSON, the label value is used
     * instead of the enum constant name.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong> <code>Compression.LZW</code> → JSON:
     * <code>"LZW"</code>
     * </p>
     * 
     * @return the compression method label
     */
    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Creates a Compression enum from a label string (case-insensitive).
     * 
     * <p>
     * Used by Jackson for JSON deserialization. Allows flexible input
     * accepting labels in any case: "LZW", "lzw", "Lzw", etc.
     * </p>
     * 
     * <p>
     * <strong>Usage Examples:</strong>
     * </p>
     * 
     * <pre>
     * Compression.fromLabel("LZW"); // Returns Compression.LZW
     * Compression.fromLabel("lzw"); // Returns Compression.LZW (case-insensitive)
     * Compression.fromLabel("Deflate"); // Returns Compression.DEFLATE
     * Compression.fromLabel("none"); // Returns Compression.NONE
     * </pre>
     * 
     * @param label the compression method label (case-insensitive)
     * @return the corresponding Compression enum value
     * @throws IllegalArgumentException if the label doesn't match any compression
     *                                  method
     */
    @JsonCreator
    public static Compression fromLabel(String label) {
        for (Compression c : values()) {
            if (c.label.equalsIgnoreCase(label)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown compression: " + label);
    }

    /**
     * Returns the default compression method.
     * 
     * <p>
     * Used when no compression is explicitly specified by the user.
     * Returns {@link #NONE} to ensure fastest processing and universal
     * compatibility
     * during development and testing.
     * </p>
     * 
     * <p>
     * <strong>Note:</strong> In production, consider using {@link #LZW} as default
     * for better file size management.
     * </p>
     * 
     * <p>
     * <strong>Usage Example:</strong>
     * </p>
     * 
     * <pre>
     * GeorefImage image = new GeorefImage();
     * if (image.getCompression() == null) {
     *     image.setCompression(Compression.getDefault());
     * }
     * </pre>
     * 
     * @return {@link #NONE} as the default compression method
     */
    public static Compression getDefault() {
        return Compression.NONE;
    }
}