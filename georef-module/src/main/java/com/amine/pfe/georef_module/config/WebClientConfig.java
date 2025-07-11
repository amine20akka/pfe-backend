package com.amine.pfe.georef_module.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${gdal.server.url}")
    private String gdalServerUrl;

    /**
     * WebClient configuré pour GDAL Server
     */
    @Bean(name = "gdalWebClient")
    public WebClient gdalWebClient() {
        return WebClient.builder()
                .baseUrl(gdalServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10 MB
                })
                .build();
    }
    
    /**
     * WebClient par défaut si aucun qualifier n'est spécifié
     * Utile pour des usages génériques
     */
    @Bean
    @Primary
    public WebClient defaultWebClient() {
        return WebClient.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10 MB
                })
                .build();
    }
}