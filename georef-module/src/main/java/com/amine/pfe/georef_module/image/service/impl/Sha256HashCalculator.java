package com.amine.pfe.georef_module.image.service.impl;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amine.pfe.georef_module.image.service.port.HashCalculator;
import com.amine.pfe.georef_module.image.util.FileUtils;

@Component
public class Sha256HashCalculator implements HashCalculator {
    @Override
    public String calculate(MultipartFile file) throws IOException {
        return FileUtils.calculateSHA256(file);
    }
}
