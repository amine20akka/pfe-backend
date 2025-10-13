package com.amine.pfe.georef_module.gcp.service.impl;

import org.springframework.stereotype.Component;

import com.amine.pfe.georef_module.entity.Gcp;
import com.amine.pfe.georef_module.entity.GeorefImage;
import com.amine.pfe.georef_module.gcp.service.port.GcpFactory;

/**
 * Default implementation of {@link GcpFactory} for creating Ground Control
 * Point entities.
 * 
 * <p>
 * This implementation provides straightforward GCP instantiation with direct
 * property
 * assignment. It's registered as a Spring component for dependency injection
 * throughout
 * the application.
 * </p>
 * 
 * <p>
 * <b>Implementation Characteristics:</b>
 * </p>
 * <ul>
 * <li><b>Stateless:</b> No internal state, thread-safe for concurrent use</li>
 * <li><b>Simple:</b> Direct property mapping without complex logic</li>
 * <li><b>Injected:</b> Registered as a Spring {@code @Component} for
 * autowiring</li>
 * </ul>
 * 
 * <p>
 * <b>Design Rationale:</b>
 * </p>
 * <ul>
 * <li>Separates object creation from business logic (Single Responsibility
 * Principle)</li>
 * <li>Facilitates testing by allowing mock factories in unit tests</li>
 * <li>Provides a single point for future enhancements (validation, logging,
 * etc.)</li>
 * <li>Follows Factory pattern for clean object instantiation</li>
 * </ul>
 * 
 * @author Amine
 * @version 1.0
 * @see GcpFactory
 * @see Gcp
 */
@Component
public class GcpFactoryImpl implements GcpFactory {

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Details:</b>
     * </p>
     * <p>
     * This implementation creates a new GCP instance and directly assigns all
     * provided
     * parameters to the corresponding entity properties. No validation or
     * transformation
     * is performed at this level.
     * </p>
     * 
     * <p>
     * <b>Property Mapping:</b>
     * </p>
     * <table border="1">
     * <tr>
     * <th>Parameter</th>
     * <th>GCP Property</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>image</td>
     * <td>image</td>
     * <td>Establishes bidirectional relationship with GeorefImage</td>
     * </tr>
     * <tr>
     * <td>sourceX</td>
     * <td>sourceX</td>
     * <td>Pixel X-coordinate in source image</td>
     * </tr>
     * <tr>
     * <td>sourceY</td>
     * <td>sourceY</td>
     * <td>Pixel Y-coordinate in source image</td>
     * </tr>
     * <tr>
     * <td>mapX</td>
     * <td>mapX</td>
     * <td>Map X-coordinate (Easting/Longitude)</td>
     * </tr>
     * <tr>
     * <td>mapY</td>
     * <td>mapY</td>
     * <td>Map Y-coordinate (Northing/Latitude)</td>
     * </tr>
     * <tr>
     * <td>index</td>
     * <td>index</td>
     * <td>Sequential identifier for ordering</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <b>Note:</b> Validation (e.g., null checks, coordinate range validation)
     * should be
     * performed by the calling service layer before invoking this factory method.
     * </p>
     * 
     * @param image   {@inheritDoc}
     * @param sourceX {@inheritDoc}
     * @param sourceY {@inheritDoc}
     * @param mapX    {@inheritDoc}
     * @param mapY    {@inheritDoc}
     * @param index   {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Gcp createGcp(GeorefImage image, Double sourceX, Double sourceY, double mapX, double mapY, Integer index) {
        Gcp gcp = new Gcp();
        gcp.setImage(image);
        gcp.setIndex(index);
        gcp.setSourceX(sourceX);
        gcp.setSourceY(sourceY);
        gcp.setMapX(mapX);
        gcp.setMapY(mapY);
        return gcp;
    }
}