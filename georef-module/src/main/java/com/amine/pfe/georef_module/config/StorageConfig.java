package com.amine.pfe.georef_module.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
@Configuration
@ConfigurationProperties(prefix = "georef.storage")
public class StorageConfig {
    
    private Path basePath;
    private Path originalDir;
    private Path georeferencedDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(basePath);
            Files.createDirectories(originalDir);
            Files.createDirectories(georeferencedDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    // Getters & Setters
    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = Paths.get(basePath);
    }

    public Path getOriginalDir() {
        return originalDir;
    }

    public void setOriginalDir(String originalDir) {
        this.originalDir = Paths.get(originalDir);
    }

    public Path getGeoreferencedDir() {
        return georeferencedDir;
    }

    public void setGeoreferencedDir(String georeferencedDir) {
        this.georeferencedDir = Paths.get(georeferencedDir);
    }
}
