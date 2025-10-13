package com.amine.pfe.georef_module.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.amine.pfe.georef_module.gcp.exceptions.DuplicateGcpIndexException;
import com.amine.pfe.georef_module.image.exceptions.UnsupportedImageFormatException;

/**
 * {@code GlobalExceptionHandler} is a centralized exception handling class
 * for the Georeferencing module, leveraging Spring's
 * {@link RestControllerAdvice}.
 *
 * <p>
 * It intercepts exceptions thrown by controllers or services and maps them
 * to meaningful HTTP responses with appropriate status codes and messages.
 *
 * <p>
 * Handled exceptions include:
 * <ul>
 * <li>{@link UnsupportedImageFormatException} → HTTP 415
 * UNSUPPORTED_MEDIA_TYPE</li>
 * <li>{@link ImageNotFoundException} → HTTP 404 NOT_FOUND</li>
 * <li>{@link DuplicateGcpIndexException} → HTTP 409 CONFLICT</li>
 * <li>{@link NotEnoughGcpException} → HTTP 400 BAD_REQUEST</li>
 * <li>{@link Exception} → HTTP 500 INTERNAL_SERVER_ERROR (fallback for
 * unhandled exceptions)</li>
 * </ul>
 *
 * <p>
 * Example usage in a controller:
 * 
 * <pre>
 * &#64;PostMapping("/georeference")
 * public ResponseEntity&lt;String&gt; georeference(@RequestBody GeorefRequest request) {
 *     georefService.georeference(request);
 *     return ResponseEntity.ok("Georeferencing completed");
 * }
 * </pre>
 * 
 * If any exception occurs, {@code GlobalExceptionHandler} automatically
 * converts it into a structured HTTP response.
 *
 * <p>
 * This approach improves API reliability and provides consistent error
 * messaging to clients or front-end applications.
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedImageFormatException.class)
    public ResponseEntity<String> handleUnsupportedImageFormatException(UnsupportedImageFormatException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Unsupported image format: " + ex.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<String> handleImageNotFoundException(ImageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Image not found: " + ex.getMessage());
    }

    @ExceptionHandler(DuplicateGcpIndexException.class)
    public ResponseEntity<String> handleDuplicateGcpIndexException(DuplicateGcpIndexException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Duplicate GCP index: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(NotEnoughGcpException.class)
    public ResponseEntity<String> handleNotEnoughGcpException(NotEnoughGcpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Not enough GCPs: " + ex.getMessage());
    }
}
