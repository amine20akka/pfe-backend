package com.amine.pfe.georef_module.gcp.exceptions;

import com.amine.pfe.georef_module.exception.ImageNotFoundException;

/**
 * Exception thrown when a requested Ground Control Point cannot be found in the
 * system.
 * 
 * <p>
 * This exception indicates that an operation attempted to access, update, or
 * delete a GCP
 * using an identifier (typically UUID) that doesn't correspond to any existing
 * GCP in the
 * database. This is a common scenario in CRUD operations where the resource may
 * have been
 * deleted by another process or the client provided an invalid ID.
 * </p>
 * 
 * <p>
 * <b>When This Exception is Thrown:</b>
 * </p>
 * <ul>
 * <li>Attempting to retrieve a GCP by ID that doesn't exist</li>
 * <li>Updating a GCP that has been deleted</li>
 * <li>Deleting a GCP that doesn't exist (idempotency consideration)</li>
 * <li>Client provides a malformed or incorrect GCP UUID</li>
 * </ul>
 * 
 * <p>
 * <b>Exception Hierarchy:</b>
 * </p>
 * <p>
 * Extends {@link RuntimeException}, making it an unchecked exception. This
 * follows Spring's
 * philosophy of not forcing exception handling for resource-not-found
 * scenarios, which are
 * typically handled by global exception handlers.
 * </p>
 * 
 * <p>
 * <b>HTTP Status Mapping:</b>
 * </p>
 * <p>
 * In a REST API context, this exception should be mapped to:
 * </p>
 * <ul>
 * <li><b>HTTP 404 Not Found:</b> Standard status for missing resources</li>
 * </ul>
 * 
 * <p>
 * <b>Client Response Example (JSON):</b>
 * </p>
 * 
 * <pre>{@code
 * {
 *   "errorCode": "GCP_NOT_FOUND",
 *   "message": "GCP with ID a3f1b2c4-5d6e-7f8g-9h0i-1j2k3l4m5n6o not found",
 *   "status": 404,
 *   "timestamp": "2025-10-16T14:30:00Z"
 * }
 * }</pre>
 * 
 * <p>
 * <b>Idempotency Consideration:</b>
 * </p>
 * <p>
 * For DELETE operations, consider whether to throw this exception:
 * </p>
 * <ul>
 * <li><b>Strict approach:</b> Throw exception if resource doesn't exist (client
 * should know)</li>
 * <li><b>Idempotent approach:</b> Return success even if already deleted
 * (DELETE is idempotent by REST standards)</li>
 * </ul>
 * 
 * <p>
 * <b>Related Exceptions:</b>
 * </p>
 * <ul>
 * <li>{@link DuplicateGcpIndexException} - When attempting to create duplicate
 * GCP indices</li>
 * <li>{@link ImageNotFoundException} - When the parent image doesn't exist</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see RuntimeException
 * @see DuplicateGcpIndexException
 */
public class GcpNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new GcpNotFoundException with the specified detail message.
     * 
     * <p>The message should clearly identify which GCP was not found (typically by ID)
     * to facilitate debugging and provide meaningful feedback to API clients.</p>
     * 
     * <p><b>Recommended Message Format:</b></p>
     * <pre>{@code
     * String message = String.format(
     *     "GCP with ID %s not found",
     *     gcpId
     * );
     * throw new GcpNotFoundException(message);
     * }</pre>
     * 
     * <p><b>Message Best Practices:</b></p>
     * <ul>
     *   <li>Include the GCP identifier (UUID) that was not found</li>
     *   <li>Optionally include the operation context ("Cannot update GCP...", "Cannot delete GCP...")</li>
     *   <li>Use clear, user-friendly language</li>
     *   <li>Avoid exposing sensitive internal details or stack traces in the message</li>
     * </ul>
     * 
     * <p><b>Contextual Messages:</b></p>
     * <pre>{@code
     * // For GET operations
     * throw new GcpNotFoundException("GCP with ID " + gcpId + " not found");
     * 
     * // For UPDATE operations
     * throw new GcpNotFoundException("Cannot update GCP " + gcpId + ": not found");
     * 
     * // For DELETE operations
     * throw new GcpNotFoundException("Cannot delete GCP " + gcpId + ": not found");
     * }</pre>
     * 
     * @param message the detail message explaining which GCP was not found and optionally the operation context
     */
    public GcpNotFoundException(String message) {
        super(message);
    }
}