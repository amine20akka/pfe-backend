package com.amine.pfe.georef_module.gcp.exceptions;

import com.amine.pfe.georef_module.exception.ImageNotFoundException;

/**
 * Exception thrown when attempting to create or update a GCP with an index that already exists
 * for the same georeferenced image.
 * 
 * <p>This exception enforces the business rule that each Ground Control Point within an image
 * must have a unique index. The index serves as a logical identifier for ordering and referencing
 * GCPs during georeferencing operations.</p>
 * 
 * <p><b>When This Exception is Thrown:</b></p>
 * <ul>
 *   <li>Creating a new GCP with an index that already exists for the image</li>
 *   <li>Updating a GCP's index to a value already used by another GCP in the same image</li>
 *   <li>Importing GCPs from an external source with duplicate indices</li>
 * </ul>
 * 
 * <p><b>Exception Hierarchy:</b></p>
 * <p>Extends {@link RuntimeException}, making it an unchecked exception. This design choice
 * allows Spring's exception handling mechanisms to automatically catch and process it without
 * requiring explicit throws declarations throughout the call stack.</p>
 * 
 * <p><b>HTTP Status Mapping:</b></p>
 * <p>In a REST API context, this exception should typically be mapped to:</p>
 * <ul>
 *   <li><b>HTTP 409 Conflict:</b> Indicates the request conflicts with existing state</li>
 * </ul>
 * 
 * <p><b>Client Response Example (JSON):</b></p>
 * <pre>{@code
 * {
 *   "errorCode": "DUPLICATE_GCP_INDEX",
 *   "message": "GCP with index 3 already exists for image a3f1b2c4-...",
 *   "status": 409,
 *   "timestamp": "2025-10-16T14:30:00Z"
 * }
 * }</pre>
 * 
 * <p><b>Related Exceptions:</b></p>
 * <ul>
 *   <li>{@link GcpNotFoundException} - When a referenced GCP doesn't exist</li>
 *   <li>{@link ImageNotFoundException} - When the parent image doesn't exist</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see RuntimeException
 * @see GcpNotFoundException
 */
public class DuplicateGcpIndexException extends RuntimeException {
    
    /**
     * Constructs a new DuplicateGcpIndexException with the specified detail message.
     * 
     * <p>The message should clearly identify which index is duplicated and for which image,
     * to facilitate debugging and provide meaningful feedback to API clients.</p>
     * 
     * <p><b>Recommended Message Format:</b></p>
     * <pre>{@code
     * String message = String.format(
     *     "GCP with index %d already exists for image %s",
     *     duplicateIndex,
     *     imageId
     * );
     * throw new DuplicateGcpIndexException(message);
     * }</pre>
     * 
     * <p><b>Message Best Practices:</b></p>
     * <ul>
     *   <li>Include the duplicate index value</li>
     *   <li>Include the image ID for context</li>
     *   <li>Use clear, user-friendly language</li>
     *   <li>Avoid exposing sensitive internal details</li>
     * </ul>
     * 
     * @param message the detail message explaining which GCP index is duplicated and for which image
     */
    public DuplicateGcpIndexException(String message) {
        super(message);
    }
}