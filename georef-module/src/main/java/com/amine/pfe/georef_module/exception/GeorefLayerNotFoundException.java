package com.amine.pfe.georef_module.exception;

/**
 * {@code GeorefLayerNotFoundException} is a custom runtime exception
 * used to indicate that a requested georeferenced layer could not be found
 * in the system or database.
 *
 * <p>
 * Typical scenarios for throwing this exception include:
 * <ul>
 * <li>The requested layer ID does not exist.</li>
 * <li>The layer has been deleted or is not yet published.</li>
 * <li>Incorrect input parameters for fetching a layer.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * GeorefLayer layer = georefService.getLayerById(layerId);
 * if (layer == null) {
 *     throw new GeorefLayerNotFoundException("Layer with ID " + layerId + " not found");
 * }
 * </pre>
 *
 * <p>
 * Being a {@link RuntimeException}, it does not require mandatory
 * try-catch blocks, but it should be handled in service or controller
 * layers to return meaningful error responses in APIs.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 */
public class GeorefLayerNotFoundException extends RuntimeException {

    /**
     * Constructs a new GeorefLayerNotFoundException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public GeorefLayerNotFoundException(String message) {
        super(message);
    }
}
