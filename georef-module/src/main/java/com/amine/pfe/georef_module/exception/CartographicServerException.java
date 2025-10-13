package com.amine.pfe.georef_module.exception;

/**
 * {@code CartographicServerException} is a custom runtime exception
 * used in the Georeferencing module to indicate errors related to
 * cartographic or map server operations.
 *
 * <p>
 * This exception is typically thrown when:
 * <ul>
 * <li>Communication with a cartographic server (e.g., GeoServer) fails.</li>
 * <li>Map layers cannot be retrieved or published.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * if (!geoServer.isAvailable()) {
 *     throw new CartographicServerException("GeoServer is not reachable");
 * }
 * </pre>
 *
 * <p>
 * Being a {@link RuntimeException}, it does not require mandatory
 * try-catch blocks, but it should be handled appropriately at higher
 * service or controller levels to provide meaningful error responses
 * in APIs or UI feedback.
 * </p>
 *
 * <p>
 * This class helps distinguish cartographic server errors from
 * other types of runtime exceptions in the application.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 */
public class CartographicServerException extends RuntimeException {

    /**
     * Constructs a new CartographicServerException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public CartographicServerException(String message) {
        super(message);
    }
}
