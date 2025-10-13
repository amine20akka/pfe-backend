package com.amine.pfe.georef_module.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class for managing storage directories for the georeferencing
 * module.
 * 
 * <p>
 * This configuration binds to application properties with the prefix
 * "georef.storage"
 * and manages three main directory paths:
 * </p>
 * <ul>
 * <li><strong>Base path</strong> - Root directory for all storage operations
 * (default: ./georef-storage)</li>
 * <li><strong>Original directory</strong> - Storage for uploaded images before
 * georeferencing (default: ./georef-storage/originals)</li>
 * <li><strong>Georeferenced directory</strong> - Storage for
 * processed/georeferenced images (default: ./georef-storage/georeferenced)</li>
 * </ul>
 * 
 * <p>
 * The directories are automatically created during application startup if they
 * don't exist.
 * This class uses relative paths by default, allowing for flexible deployment
 * across different environments.
 * </p>
 * 
 * <p>
 * <strong>Current application.properties configuration:</strong>
 * </p>
 * 
 * <pre>
 * georef.storage.base-path=./georef-storage
 * georef.storage.original-dir=${georef.storage.base-path}/originals
 * georef.storage.georeferenced-dir=${georef.storage.base-path}/georeferenced
 * </pre>
 * 
 * <p>
 * <strong>File upload limits:</strong>
 * </p>
 * <ul>
 * <li>Maximum file size: 10MB</li>
 * <li>Maximum request size: 10MB</li>
 * </ul>
 * 
 * <p>
 * <strong>Integration:</strong> This configuration works in conjunction with:
 * <ul>
 * <li>GDAL server (http://localhost:5000) for georeferencing processing</li>
 * <li>GeoServer (http://localhost:8080/geoserver) for serving georeferenced
 * layers</li>
 * <li>RabbitMQ (image.processing.queue) for asynchronous image processing</li>
 * </ul>
 * </p>
 * 
 * @author Amine
 * @version 1.0
 * @since 1.0
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@Configuration
@ConfigurationProperties(prefix = "georef.storage")
public class StorageConfig {

    /**
     * The base path for all storage operations.
     * 
     * <p>
     * Acts as the root directory for the georeferencing file system.
     * Configured via <code>georef.storage.base-path</code> property.
     * </p>
     * 
     * <p>
     * Default: <code>./georef-storage</code> (relative to application working
     * directory)
     * </p>
     */
    private Path basePath;

    /**
     * Directory path for storing original uploaded images.
     * 
     * <p>
     * Images are stored here before any georeferencing processing occurs.
     * This directory preserves the original uploaded files for reference and
     * reprocessing.
     * </p>
     * 
     * <p>
     * Configured via <code>georef.storage.original-dir</code> property.
     * </p>
     * <p>
     * Default: <code>./georef-storage/originals</code>
     * </p>
     */
    private Path originalDir;

    /**
     * Directory path for storing georeferenced/processed images.
     * 
     * <p>
     * Images are saved here after successful georeferencing by the GDAL server.
     * These processed files are typically published to GeoServer for map serving.
     * </p>
     * 
     * <p>
     * Configured via <code>georef.storage.georeferenced-dir</code> property.
     * </p>
     * <p>
     * Default: <code>./georef-storage/georeferenced</code>
     * </p>
     */
    private Path georeferencedDir;

    /**
     * Initializes the storage directories after bean construction.
     * 
     * <p>
     * This method is automatically called by Spring after all properties have been
     * set
     * and dependency injection is complete. It creates all required directories
     * (basePath, originalDir, georeferencedDir) if they don't already exist.
     * Parent directories are created as needed using
     * {@link Files#createDirectories(Path, java.nio.file.attribute.FileAttribute...)}.
     * </p>
     * 
     * <p>
     * <strong>Execution order:</strong>
     * </p>
     * <ol>
     * <li>Creates base directory (./georef-storage)</li>
     * <li>Creates original images directory (./georef-storage/originals)</li>
     * <li>Creates georeferenced images directory
     * (./georef-storage/georeferenced)</li>
     * </ol>
     * 
     * @throws RuntimeException if directory creation fails due to I/O errors,
     *                          permission issues,
     *                          or insufficient disk space. The application startup
     *                          will fail if this exception is thrown.
     */
    @PostConstruct
    public void init() {
        try {
            // Create all necessary directories including parent directories
            Files.createDirectories(basePath);
            Files.createDirectories(originalDir);
            Files.createDirectories(georeferencedDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    // Getters & Setters

    /**
     * Returns the base path for storage operations.
     * 
     * @return the base directory path as a {@link Path} object, or null if not yet
     *         configured
     */
    public Path getBasePath() {
        return basePath;
    }

    /**
     * Sets the base path for storage operations.
     * 
     * <p>
     * This method is called by Spring during property binding from
     * application.properties
     * and converts the string path to a {@link Path} object. Supports both relative
     * and absolute paths.
     * </p>
     * 
     * @param basePath the base directory path as a string (e.g., "./georef-storage"
     *                 or "/var/app/storage")
     */
    public void setBasePath(String basePath) {
        this.basePath = Paths.get(basePath);
    }

    /**
     * Returns the directory path for original uploaded images.
     * 
     * @return the original images directory path as a {@link Path} object, or null
     *         if not yet configured
     */
    public Path getOriginalDir() {
        return originalDir;
    }

    /**
     * Sets the directory path for storing original uploaded images.
     * 
     * <p>
     * This method is called by Spring during property binding from
     * application.properties
     * and converts the string path to a {@link Path} object. Typically uses
     * property placeholder
     * resolution (e.g., ${georef.storage.base-path}/originals).
     * </p>
     * 
     * @param originalDir the original images directory path as a string
     */
    public void setOriginalDir(String originalDir) {
        this.originalDir = Paths.get(originalDir);
    }

    /**
     * Returns the directory path for georeferenced images.
     * 
     * @return the georeferenced images directory path as a {@link Path} object, or
     *         null if not yet configured
     */
    public Path getGeoreferencedDir() {
        return georeferencedDir;
    }

    /**
     * Sets the directory path for storing georeferenced images.
     * 
     * <p>
     * This method is called by Spring during property binding from
     * application.properties
     * and converts the string path to a {@link Path} object. Typically uses
     * property placeholder
     * resolution (e.g., ${georef.storage.base-path}/georeferenced).
     * </p>
     * 
     * @param georeferencedDir the georeferenced images directory path as a string
     */
    public void setGeoreferencedDir(String georeferencedDir) {
        this.georeferencedDir = Paths.get(georeferencedDir);
    }
}