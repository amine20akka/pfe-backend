package com.amine.pfe.georef_module.gcp.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Request DTO for bulk loading/importing Ground Control Points.
 * 
 * <p>
 * This DTO encapsulates the payload for operations that load multiple GCPs at
 * once,
 * such as importing from external files (JSON)
 * </p>
 * 
 * <p>
 * <b>Use Cases:</b>
 * </p>
 * <ul>
 * <li>Importing GCPs from a JSON file</li>
 * <li>Restoring GCPs from a previous georeferencing session</li>
 * <li>Updating all GCPs for an image in a single transaction</li>
 * </ul>
 * 
 * <p>
 * <b>Overwrite Behavior:</b>
 * </p>
 * <ul>
 * <li><b>overwrite = true:</b> Deletes existing GCPs before loading new ones
 * (full replacement)</li>
 * <li><b>overwrite = false:</b> Appends new GCPs to existing ones
 * (merge/add)</li>
 * </ul>
 * 
 * <p>
 * <b>Example Usage:</b>
 * </p>
 * 
 * <pre>{@code
 * @PostMapping("/images/{imageId}/gcps/load")
 * public ResponseEntity<List<GcpDto>> loadGcps(@RequestBody LoadGcpsRequest request) {
 *     return ResponseEntity.ok(gcpService.loadGcps(request));
 * }
 * }</pre>
 * 
 * <p>
 * <b>JSON Example:</b>
 * </p>
 * 
 * <pre>{@code
 * {
 *   "imageId": "a3f1b2c4-5d6e-7f8g-9h0i-1j2k3l4m5n6o",
 *   "gcps": [
 *     {"sourceX": 100.5, "sourceY": 200.3, "mapX": 567832.45, "mapY": 4123456.78, "index": 1},
 *     {"sourceX": 450.2, "sourceY": 350.8, "mapX": 568012.33, "mapY": 4123612.90, "index": 2}
 *   ],
 *   "overwrite": true
 * }
 * }</pre>
 * 
 * @author Amine
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class LoadGcpsRequest {

    /** The ID of the georeferenced image to load GCPs for. */
    private UUID imageId;

    /**
     * List of GCPs to load. Can contain new GCPs (id=null) or existing ones for
     * update.
     */
    private List<GcpDto> gcps;

    /**
     * Whether to delete existing GCPs before loading.
     * <ul>
     * <li>true: Replace all existing GCPs (delete then insert)</li>
     * <li>false: Keep existing GCPs and add new ones</li>
     * </ul>
     */
    private boolean overwrite;
}