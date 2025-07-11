package com.amine.pfe.georef_module.image.service.port;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface HashCalculator {
    String calculate(MultipartFile file) throws IOException;
}
