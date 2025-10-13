package com.amine.pfe.georef_module.enums;

import lombok.Data;

/**
 * {@code GeorefSettings} is a configuration class that encapsulates
 * all settings required for georeferencing a raster image within the
 * WebGIS georeferencing module.
 *
 * <p>
 * This class combines multiple aspects of georeferencing, including:
 * <ul>
 * <li>Output file naming</li>
 * <li>Transformation type (polynomial degree)</li>
 * <li>Spatial reference system (SRID)</li>
 * <li>Resampling method (interpolation type)</li>
 * <li>Compression type (for GeoTIFF output)</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * GeorefSettings settings = new GeorefSettings();
 * settings.setOutputFilename("orthophoto_001.tif");
 * settings.setTransformationType(TransformationType.POLYNOMIALE_2);
 * settings.setSrid(Srid._4326);
 * settings.setResamplingMethod(ResamplingMethod.BILINEAR);
 * settings.setCompressionType(Compression.LZW);
 * </pre>
 *
 * <p>
 * This object can be serialized/deserialized in JSON for API requests
 * or stored in configuration files. It serves as the central configuration
 * object for the georeferencing workflow, passed to processing services
 * that integrate with GDAL or other raster processing libraries.
 *
 * <p>
 * <strong>Integration Example:</strong>
 * </p>
 * 
 * <pre>
 * GeorefService georefService = new GeorefService();
 * georefService.georeference(inputFile, settings);
 * </pre>
 *
 * <p>
 * <strong>Notes:</strong>
 * </p>
 * <ul>
 * <li>If any property is not specified, default enum values are applied:
 * <ul>
 * <li>TransformationType → POLYNOMIALE_1</li>
 * <li>Srid → _3857</li>
 * <li>ResamplingMethod → NEAREST</li>
 * <li>Compression → NONE</li>
 * </ul>
 * </li>
 * <li>The output filename should include the file extension (e.g., .tif)</li>
 * </ul>
 *
 * <p>
 * This class uses Lombok's {@link Data} annotation to automatically
 * generate getters, setters, equals, hashCode, and toString methods.
 * </p>
 *
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see TransformationType
 * @see Srid
 * @see ResamplingMethod
 * @see Compression
 */
@Data
public class GeorefSettings {

    /**
     * Name of the output georeferenced file, including extension (e.g.,
     * "image.tif").
     */
    private String outputFilename;

    /** Polynomial transformation type to apply during georeferencing. */
    private TransformationType transformationType;

    /** Spatial Reference System Identifier (SRID) for the output raster. */
    private Srid srid;

    /** Resampling method used for pixel interpolation during transformation. */
    private ResamplingMethod resamplingMethod;

    /** Compression method for the output GeoTIFF file. */
    private Compression compressionType;
}
