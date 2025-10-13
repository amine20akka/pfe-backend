package com.amine.pfe.georef_module.exception;

/**
 * {@code NotEnoughGcpException} is a custom runtime exception thrown when
 * a georeferencing operation does not have a sufficient number of
 * Ground Control Points (GCPs) to perform the transformation.
 *
 * <p>
 * Typical scenarios for throwing this exception include:
 * <ul>
 * <li>User provides too few GCPs for the selected transformation type.</li>
 * <li>Data integrity errors or incomplete datasets.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * if (gcps.size() &lt; requiredGcpCount) {
 *     throw new NotEnoughGcpException("At least " + requiredGcpCount + " GCPs are required");
 * }
 * </pre>
 *
 * <p>
 * Being a {@link RuntimeException}, it does not require mandatory
 * try-catch blocks, but it should be handled appropriately at higher
 * service or controller layers to provide meaningful error responses.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 */
public class NotEnoughGcpException extends RuntimeException {

    /**
     * Constructs a new NotEnoughGcpException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public NotEnoughGcpException(String message) {
        super(message);
    }
}
