package com.amine.pfe.georef_module.image.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.amine.pfe.georef_module.image.dto.PublicationResponse;
import com.amine.pfe.georef_module.image.service.port.CartographicServer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeoserverAdapter implements CartographicServer {

    @Value("${geoserver.url}")
    private String GEOSERVER_URL;

    @Value("${geoserver.workspace}")
    private String WORKSPACE;

    @Value("${geoserver.username}")
    private String USER;

    @Value("${geoserver.password}")
    private String PASSWORD;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PublicationResponse publishGeoTiff(String georeferencedImagePath, String layerName, String storeName) {
        try {
            boolean storeCreated = createCoverageStore(storeName, georeferencedImagePath);
            boolean layerPublished = publishCoverageLayer(storeName, layerName);

            if (storeCreated && layerPublished) {
                return new PublicationResponse(
                        WORKSPACE,
                        storeName,
                        layerName,
                        generateWmsUrl(layerName));
            } else {
                throw new RuntimeException("Publication GeoTIFF échouée");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la publication GeoTIFF", e);
            throw new RuntimeException("Erreur publication GeoTIFF", e);
        }
    }

    private boolean createCoverageStore(String storeName, String filePath) {
        String uri = GEOSERVER_URL + String.format("/rest/workspaces/%s/coveragestores", WORKSPACE);

        String payload = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<coverageStore>" +
                        "  <name>%s</name>" +
                        "  <type>GeoTIFF</type>" +
                        "  <enabled>true</enabled>" +
                        "  <url>file:C:/Users/aakkari/Downloads/pfe-backend/%s</url>" +
                        "  <workspace><name>%s</name></workspace>" +
                        "</coverageStore>",
                storeName, filePath, WORKSPACE);

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Erreur création du coverageStore", e);
            return false;
        }
    }

    private boolean publishCoverageLayer(String storeName, String layerName) {
        String uri = GEOSERVER_URL
                + String.format("/rest/workspaces/%s/coveragestores/%s/coverages", WORKSPACE, storeName);

        String payload = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<coverage>" +
                        "  <name>%s</name>" +
                        "  <enabled>true</enabled>" +
                        "</coverage>",
                layerName);

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Erreur publication de la couverture", e);
            return false;
        }
    }

    @Override
    public boolean deleteGeoTiffLayer(String layerName, String storeName) {
        try {
            boolean layerDeleted = deleteLayer(layerName);
            Thread.sleep(100);

            boolean coverageDeleted = deleteCoverage(layerName, storeName);
            Thread.sleep(100);

            boolean storeDeleted = deleteCoverageStore(storeName);
            Thread.sleep(100);

            return layerDeleted && coverageDeleted && storeDeleted;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression complète de la couche GeoTIFF", e);
            return false;
        }
    }

    private boolean deleteLayer(String layerName) {
        String uri = GEOSERVER_URL + String.format("/rest/layers/%s:%s", WORKSPACE, layerName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Erreur suppression layer", e);
            return false;
        }
    }

    private boolean deleteCoverage(String layerName, String storeName) {
        String uri = GEOSERVER_URL
                + String.format("/rest/workspaces/%s/coveragestores/%s/coverages/%s", WORKSPACE, storeName, layerName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Erreur suppression coverage", e);
            return false;
        }
    }

    private boolean deleteCoverageStore(String storeName) {
        String uri = GEOSERVER_URL
                + String.format("/rest/workspaces/%s/coveragestores/%s?recurse=true", WORKSPACE, storeName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Erreur suppression store", e);
            return false;
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String credentials = USER + ":" + PASSWORD;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }

    private String generateWmsUrl(String layerName) {
        return UriComponentsBuilder.fromUriString(GEOSERVER_URL)
                .path("/ows")
                .queryParam("service", "WMS")
                .queryParam("version", "1.3.0")
                .queryParam("request", "GetCapabilities")
                .queryParam("layers", WORKSPACE + ":" + layerName)
                .build()
                .toUriString();
    }
}
