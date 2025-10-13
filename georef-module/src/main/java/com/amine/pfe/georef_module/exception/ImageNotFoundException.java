package com.amine.pfe.georef_module.exception;

/**
 * {@code ImageNotFoundException} is a custom runtime exception
 * used to indicate that a requested image (original or georeferenced)
 * could not be found in the system or storage.
 *
 * <p>
 * Typical scenarios for throwing this exception include:
 * <ul>
 * <li>The requested image file does not exist in the storage directory.</li>
 * <li>The image ID does not match any database record.</li>
 * <li>Accessing a deleted or unavailable image.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * Image img = imageService.getImageById(imageId);
 * if (img == null) {
 *     throw new ImageNotFoundException("Image with ID " + imageId + " not found");
 * }
 * </pre>
 *
 * <p>
 * This is a {@link RuntimeException}, so it does not require mandatory
 * try-catch blocks. Proper handling in service or controller layers
 * ensures meaningful API responses.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 */
public class ImageNotFoundException extends RuntimeException {

    /**
     * Constructs a new ImageNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ImageNotFoundException(String message) {
        super(message);
    }
}
