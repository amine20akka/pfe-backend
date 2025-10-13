package com.amine.pfe.georef_module.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration representing the lifecycle status of a georeferencing image.
 * 
 * <p>
 * This enum tracks an image through the complete georeferencing workflow,
 * from initial upload to final completion or failure. Each status represents
 * a distinct phase in the process and determines what actions can be performed
 * on the image.
 * </p>
 * 
 * <p>
 * <strong>Complete Workflow Diagram:</strong>
 * </p>
 * 
 * <pre>
 *.                   ┌─────────────┐
 *                    │   UPLOADED  │ ← Image uploaded to system
 *                    └──────┬──────┘
 *                           │
 *                           ↓
 *                    ┌─────────────┐
 *                    │   PENDING   │ ← GCPs added, ready for processing
 *                    └──────┬──────┘
 *                           │
 *                           ↓
 *                    ┌─────────────┐
 *                    │ PROCESSING  │ ← GDAL server is georeferencing
 *                    └──────┬──────┘
 *                           │
 *                  ┌────────┴────────┐
 *                  ↓                 ↓
 *           ┌─────────────┐   ┌─────────────┐
 *           │  COMPLETED  │   │   FAILED    │
 *           └─────────────┘   └─────────────┘
 *                  │                 │
 *                  ↓                 │
 *           [Published to            │
 *            GeoServer]              │
 *                                    ↓
 *                             [Can retry with
 *                              new GCPs or
 *                              parameters]
 * </pre>
 * 
 * <p>
 * <strong>Status Transitions:</strong>
 * </p>
 * <table border="1">
 * <tr>
 * <th>From Status</th>
 * <th>To Status</th>
 * <th>Trigger</th>
 * </tr>
 * <tr>
 * <td>UPLOADED</td>
 * <td>PENDING</td>
 * <td>User submits GCPs for georeferencing</td>
 * </tr>
 * <tr>
 * <td>PENDING</td>
 * <td>PROCESSING</td>
 * <td>RabbitMQ consumer picks up processing message</td>
 * </tr>
 * <tr>
 * <td>PROCESSING</td>
 * <td>COMPLETED</td>
 * <td>GDAL server successfully georeferences image</td>
 * </tr>
 * <tr>
 * <td>PROCESSING</td>
 * <td>FAILED</td>
 * <td>GDAL error, invalid GCPs, or system error</td>
 * </tr>
 * <tr>
 * <td>FAILED</td>
 * <td>PENDING</td>
 * <td>User modifies GCPs and retries</td>
 * </tr>
 * <tr>
 * <td>COMPLETED</td>
 * <td>PENDING</td>
 * <td>User wants to re-georeference with new parameters</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <strong>Integration Points:</strong>
 * </p>
 * <ul>
 * <li><strong>Database:</strong> Stored in <code>georef_images.status</code>
 * column</li>
 * <li><strong>RabbitMQ:</strong> Status updates sent as messages to notify
 * frontend</li>
 * <li><strong>REST API:</strong> Returned in image metadata endpoints</li>
 * <li><strong>Frontend:</strong> Used to display progress indicators and
 * enable/disable actions</li>
 * </ul>
 * 
 * <p>
 * <strong>JSON Serialization:</strong>
 * </p>
 * 
 * <pre>
 * // Serialization (Java → JSON)
 * GeorefStatus.PROCESSING → "processing"
 * 
 * // Deserialization (JSON → Java)
 * "pending" → GeorefStatus.PENDING (case-insensitive)
 * </pre>
 * 
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 * 
 * <pre>
 * // Setting status when image is uploaded
 * GeorefImage image = new GeorefImage();
 * image.setStatus(GeorefStatus.UPLOADED);
 * 
 * // Checking if image can be georeferenced
 * if (image.getStatus() == GeorefStatus.PENDING) {
 *     sendToProcessingQueue(image);
 * }
 * 
 * // Handling status from JSON
 * GeorefStatus status = GeorefStatus.fromLabel("completed");
 * 
 * // Checking if processing is in progress
 * boolean isProcessing = image.getStatus() == GeorefStatus.PROCESSING;
 * </pre>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see com.amine.pfe.georef_module.entity.GeorefImage
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum GeorefStatus {

    /**
     * Image has been successfully uploaded but no GCPs have been added yet.
     * 
     * <p>
     * <strong>Description:</strong>
     * </p>
     * <p>
     * This is the initial state immediately after an image is uploaded to the
     * system.
     * The image file is stored in the originals directory and a database record is
     * created,
     * but no georeferencing configuration or Ground Control Points exist yet.
     * </p>
     * 
     * <p>
     * <strong>State Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Original image file saved in:
     * <code>./georef-storage/originals/</code></li>
     * <li>Database record created with basic metadata (hash, filepath, upload
     * date)</li>
     * <li>GCP list is empty</li>
     * <li>No transformation parameters set (SRID, transformation type, etc.)</li>
     * </ul>
     * 
     * <p>
     * <strong>Available Actions:</strong>
     * </p>
     * <ul>
     * <li>Add Ground Control Points (GCPs)</li>
     * <li>Set georeferencing parameters (SRID, transformation type, resampling
     * method)</li>
     * <li>Delete the image</li>
     * </ul>
     * 
     * <p>
     * <strong>Next Status:</strong> {@link #PENDING} (when user submits GCPs for
     * processing)
     * </p>
     * 
     * <p>
     * <strong>User Experience:</strong>
     * </p>
     * <ul>
     * <li>Frontend displays: "Waiting for Ground Control Points"</li>
     * <li>GCP editor interface is available</li>
     * <li>Process button is disabled until minimum GCPs are added</li>
     * </ul>
     */
    UPLOADED("uploaded"),

    /**
     * Image has GCPs and parameters configured, waiting in queue for processing.
     * 
     * <p>
     * <strong>Description:</strong>
     * </p>
     * <p>
     * The image is ready for georeferencing with all required data configured.
     * A message has been sent to the RabbitMQ queue and the image is waiting for
     * a consumer to pick it up and start processing.
     * </p>
     * 
     * <p>
     * <strong>State Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Minimum required GCPs have been added (3+ for polynomial-1, 6+ for
     * polynomial-2, etc.)</li>
     * <li>All georeferencing parameters are set (SRID, transformation type,
     * resampling, compression)</li>
     * <li>Message sent to RabbitMQ queue: <code>image.processing.queue</code></li>
     * <li>Waiting for available worker to process the request</li>
     * </ul>
     * 
     * <p>
     * <strong>Available Actions:</strong>
     * </p>
     * <ul>
     * <li>Cancel processing request (before picked up by worker)</li>
     * <li>View image details and GCP configuration</li>
     * <li>Monitor queue position (if queue management implemented)</li>
     * </ul>
     * 
     * <p>
     * <strong>Next Status:</strong>
     * </p>
     * <ul>
     * <li>{@link #PROCESSING} - When RabbitMQ consumer picks up the message</li>
     * <li>{@link #UPLOADED} - If user cancels the request</li>
     * </ul>
     * 
     * <p>
     * <strong>Typical Duration:</strong> Seconds to minutes, depending on queue
     * length and worker availability
     * </p>
     * 
     * <p>
     * <strong>User Experience:</strong>
     * </p>
     * <ul>
     * <li>Frontend displays: "Waiting in processing queue..."</li>
     * <li>Progress indicator shows "queued" state</li>
     * </ul>
     */
    PENDING("pending"),

    /**
     * Image is currently being processed by the GDAL server.
     * 
     * <p>
     * <strong>Description:</strong>
     * </p>
     * <p>
     * The georeferencing operation is actively running on the GDAL server.
     * The server is applying the spatial transformation based on the provided GCPs
     * and generating the georeferenced GeoTIFF output file.
     * </p>
     * 
     * <p>
     * <strong>State Characteristics:</strong>
     * </p>
     * <ul>
     * <li>RabbitMQ consumer has picked up the processing message</li>
     * <li>GDAL server (Flask API at http://localhost:5000) is executing
     * gdal_translate or gdalwarp</li>
     * <li>Transformation matrix is being calculated from GCPs</li>
     * <li>Output GeoTIFF is being generated with specified parameters</li>
     * </ul>
     * 
     * <p>
     * <strong>GDAL Operations Performed:</strong>
     * </p>
     * <ol>
     * <li>Load original image from <code>./georef-storage/originals/</code></li>
     * <li>Apply Ground Control Points to calculate transformation</li>
     * <li>Calculate residual errors for quality assessment</li>
     * <li>Warp/transform the image to the target coordinate system</li>
     * <li>Apply resampling method (bilinear, cubic, etc.)</li>
     * <li>Apply compression (LZW, DEFLATE, etc.)</li>
     * <li>Save georeferenced output to
     * <code>./georef-storage/georeferenced/</code></li>
     * </ol>
     * 
     * <p>
     * <strong>Next Status:</strong>
     * </p>
     * <ul>
     * <li>{@link #COMPLETED} - Successful georeferencing</li>
     * <li>{@link #FAILED} - GDAL error, invalid parameters, or system failure</li>
     * </ul>
     * 
     * <p>
     * <strong>Typical Duration:</strong> Seconds to minutes, depending on:
     * <ul>
     * <li>Image size (larger images take longer)</li>
     * <li>Number of GCPs (more GCPs = more computation)</li>
     * <li>Transformation complexity (polynomial-3 slower than polynomial-1)</li>
     * <li>Resampling method (cubic slower than bilinear)</li>
     * <li>Server load and available resources</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>User Experience:</strong>
     * </p>
     * <ul>
     * <li>Frontend displays: "Georeferencing in progress..."</li>
     * <li>Animated progress indicator or spinner</li>
     * <li>All editing actions disabled during processing</li>
     * </ul>
     */
    PROCESSING("processing"),

    /**
     * Georeferencing has completed successfully.
     * 
     * <p>
     * <strong>Description:</strong>
     * </p>
     * <p>
     * The GDAL server has successfully georeferenced the image and saved the output
     * GeoTIFF file. The image is automatically published to GeoServer. 
     * </p>
     * 
     * <p>
     * <strong>State Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Georeferenced GeoTIFF file saved in:
     * <code>./georef-storage/georeferenced/</code></li>
     * <li>File includes embedded spatial reference (CRS/SRID)</li>
     * <li>Residual errors calculated and stored for each GCP</li>
     * <li>Mean residual error calculated for overall accuracy assessment</li>
     * <li>File path stored in <code>filepathGeoreferenced</code></li>
     * <li>Processing date stored in <code>lastGeoreferencingDate</code></li>
     * </ul>
     * 
     * <p>
     * <strong>Output File Details:</strong>
     * </p>
     * <ul>
     * <li>Format: GeoTIFF (.tif)</li>
     * <li>Coordinate System: As specified by SRID parameter</li>
     * <li>Compression: As specified by compression parameter</li>
     * <li>Contains geotransformation matrix in TIFF tags</li>
     * <li>Compatible with all major GIS software (QGIS, ArcGIS, etc.)</li>
     * </ul>
     * 
     * <p>
     * <strong>Available Actions:</strong>
     * </p>
     * <ul>
     * <li>View residual errors and quality metrics</li>
     * <li>Re-georeference with modified GCPs or parameters</li>
     * <li>Export metadata and GCP coordinates</li>
     * </ul>
     * 
     * <p>
     * <strong>Quality Assessment:</strong>
     * </p>
     * <p>
     * Check the <code>meanResidual</code> field to assess accuracy:
     * </p>
     * <ul>
     * <li>&lt; 1 pixel: Excellent accuracy ✅</li>
     * <li>1-2 pixels: Good accuracy ✅</li>
     * <li>2-3 pixels: Acceptable for most purposes ⚠️</li>
     * <li> 3 pixels: Consider adding more GCPs or checking placement ❌</li>
     * </ul>
     * 
     * <p>
     * <strong>Next Status:</strong>
     * </p>
     * <ul>
     * <li>{@link #PROCESSING} - If user wants to re-georeference with new
     * parameters</li>
     * <li>No status change - Image remains COMPLETED (terminal state unless
     * reprocessed)</li>
     * </ul>
     * 
     * <p>
     * <strong>User Experience:</strong>
     * </p>
     * <ul>
     * <li>Frontend displays: "Georeferencing completed successfully ✓"</li>
     * <li>Success notification with accuracy metrics</li>
     * <li>Quality report with residual errors displayed</li>
     * </ul>
     */
    COMPLETED("completed"),

    /**
     * Georeferencing process has failed.
     * 
     * <p>
     * <strong>Description:</strong>
     * </p>
     * <p>
     * An error occurred during the georeferencing process, preventing successful
     * completion.
     * The original image remains intact, but no georeferenced output was produced.
     * </p>
     * 
     * <p>
     * <strong>Common Failure Causes:</strong>
     * </p>
     * <ul>
     * <li><strong>Invalid coordinates:</strong> Map coordinates are outside valid
     * range for the specified SRID</li>
     * <li><strong>Incorrect SRID:</strong> Coordinate system doesn't match the
     * provided coordinates</li>
     * <li><strong>File I/O errors:</strong> Unable to read original or write
     * georeferenced file</li>
     * <li><strong>GDAL errors:</strong> Internal GDAL processing failure</li>
     * <li><strong>System errors:</strong> Out of memory, disk full, network
     * timeout</li>
     * <li><strong>Invalid image format:</strong> Original file is corrupted or
     * unsupported</li>
     * </ul>
     * 
     * <p>
     * <strong>State Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Original image remains unchanged in originals directory</li>
     * <li>No georeferenced output file created</li>
     * <li>Error message and stack trace logged</li>
     * <li>Processing attempt recorded with timestamp</li>
     * <li>All configuration data preserved for retry</li>
     * </ul>
     * 
     * <p>
     * <strong>Available Actions:</strong>
     * </p>
     * <ul>
     * <li>View detailed error message and logs</li>
     * <li>Modify GCPs (add more, improve distribution, fix coordinates)</li>
     * <li>Change transformation type to simpler method (e.g., polynomial-3 →
     * polynomial-1)</li>
     * <li>Verify and correct SRID</li>
     * <li>Check coordinate values for validity</li>
     * <li>Retry processing after corrections</li>
     * </ul>
     * 
     * <p>
     * <strong>Recovery Steps:</strong>
     * </p>
     * <ol>
     * <li>Review error message to identify root cause</li>
     * <li>Check GCP count and distribution across image</li>
     * <li>Verify coordinate values match selected SRID</li>
     * <li>Ensure minimum GCP requirements are met</li>
     * <li>Make necessary corrections</li>
     * <li>Change status back to {@link #PENDING} and retry</li>
     * </ol>
     * 
     * <p>
     * <strong>Next Status:</strong>
     * </p>
     * <ul>
     * <li>{@link #PENDING} - After user fixes issues and retries</li>
     * <li>{@link #UPLOADED} - If user wants to start over with new GCPs</li>
     * </ul>
     * 
     * <p>
     * <strong>User Experience:</strong>
     * </p>
     * <ul>
     * <li>Frontend displays: "Georeferencing failed ✗"</li>
     * <li>Error notification with specific failure reason</li>
     * <li>Retry button enabled after corrections</li>
     * </ul>
     * 
     * <p>
     * <strong>Error Handling Best Practices:</strong>
     * </p>
     * <ul>
     * <li>Log full error details including stack trace</li>
     * <li>Preserve all user configuration (GCPs, parameters)</li>
     * <li>Provide actionable error messages to users</li>
     * <li>Implement retry logic with exponential backoff for transient errors</li>
     * <li>Clean up any partial output files</li>
     * </ul>
     */
    FAILED("failed");

    /**
     * Human-readable label for the status.
     * 
     * <p>
     * Used for JSON serialization/deserialization and logging.
     * Lowercase for consistency with REST API conventions.
     * </p>
     */
    private final String label;

    /**
     * Constructor for GeorefStatus enum values.
     * 
     * @param label the human-readable label for this status
     */
    GeorefStatus(String label) {
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
     * <strong>Example:</strong> <code>GeorefStatus.PROCESSING</code> → JSON:
     * <code>"processing"</code>
     * </p>
     * 
     * @return the status label in lowercase
     */
    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Creates a GeorefStatus enum from a label string (case-insensitive).
     * 
     * <p>
     * Used by Jackson for JSON deserialization. Allows flexible input
     * accepting labels in any case: "PENDING", "pending", "Pending", etc.
     * </p>
     * 
     * <p>
     * <strong>Usage Examples:</strong>
     * </p>
     * 
     * <pre>
     * GeorefStatus.fromLabel("uploaded"); // Returns GeorefStatus.UPLOADED
     * GeorefStatus.fromLabel("PROCESSING"); // Returns GeorefStatus.PROCESSING
     * GeorefStatus.fromLabel("Completed"); // Returns GeorefStatus.COMPLETED
     * GeorefStatus.fromLabel("failed"); // Returns GeorefStatus.FAILED
     * </pre>
     * 
     * @param label the status label (case-insensitive)
     * @return the corresponding GeorefStatus enum value
     * @throws IllegalArgumentException if the label doesn't match any known status
     */
    @JsonCreator
    public static GeorefStatus fromLabel(String label) {
        for (GeorefStatus method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown georef status: " + label);
    }
}