package com.amine.pfe.georef_module.image.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.amine.pfe.georef_module.enums.GeorefSettings;
import com.amine.pfe.georef_module.gcp.dto.GcpDto;
import com.amine.pfe.georef_module.image.service.port.GeospatialServer;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class GdalAdapter implements GeospatialServer {

    @Value("${gdal.server.url}")
    private String gdalServerUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    public GdalAdapter(@Qualifier("gdalWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public InputStream processGeoref(File imageFile, List<GcpDto> gcps, GeorefSettings settings) {
        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

            String gcpsJson = objectMapper.writeValueAsString(gcps);
            String settingsJson = objectMapper.writeValueAsString(settings);

            bodyBuilder.part("gcps", gcpsJson, MediaType.APPLICATION_JSON);
            bodyBuilder.part("settings", settingsJson, MediaType.APPLICATION_JSON);
            bodyBuilder.part("image", new FileSystemResource(imageFile));

            InputStream resultInputStream = webClient.post()
                    .uri("/georef")
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> {
                                return Mono.error(new RuntimeException("Erreur client: " + response.statusCode()));
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> {
                                return Mono.error(new RuntimeException("Erreur serveur: " + response.statusCode()));
                            })
                    .bodyToMono(InputStreamResource.class)
                    .block()
                    .getInputStream();

            return resultInputStream;

        } catch (IOException e) {
            throw new RuntimeException("Erreur de préparation de la requête", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du géoréférencement", e);
        }
    }

    @Override
    public boolean deleteGeorefFile(String filename) {
        log.debug("Suppression du fichier géoréférencé: {}", filename);
        
        try {
            return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                    .path("/delete-georef")
                    .queryParam("filename", filename)
                    .build())
                .retrieve()
                .bodyToMono(DeleteResponse.class)
                .map(DeleteResponse::isSuccess)
                .doOnSuccess(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        log.debug("Fichier supprimé avec succès: {}", filename);
                    } else {
                        log.warn("Échec de la suppression du fichier: {}", filename);
                    }
                })
                .doOnError(error -> log.error("Erreur lors de la suppression du fichier {}: {}", 
                        filename, error.getMessage()))
                .onErrorReturn(false)
                .block();
        } catch (Exception e) {
            log.error("Exception lors de la suppression du fichier {}: {}", filename, e.getMessage());
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeleteResponse {
        private boolean success;
        private String message;
    }
}