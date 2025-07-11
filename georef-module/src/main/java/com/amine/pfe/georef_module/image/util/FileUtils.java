package com.amine.pfe.georef_module.image.util;

import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    public static String calculateSHA256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du hash du fichier", e);
        }
    }

    public static String normalizeOutputFilename(String name, String fallbackBaseName) {
        if (name == null || name.trim().isEmpty()) {
            return fallbackBaseName.replaceAll("\\.", "") + "_georef.tif";
        }

        int lastDotIndex = name.lastIndexOf('.');
        String baseName = (lastDotIndex != -1) ? name.substring(0, lastDotIndex) : name;

        String cleanedBaseName = baseName.replaceAll("\\.", "");

        return cleanedBaseName + ".tif";
    }

    public static String extractBaseName(String filepathOriginal) {
        String filename = Paths.get(filepathOriginal).getFileName().toString();
        int lastDotIndex = filename.lastIndexOf('.');
        String base = (lastDotIndex != -1) ? filename.substring(0, lastDotIndex) : filename;
        return base.replaceAll("\\.", "");
    }

    /**
     * Normalise le nom de fichier pour la publication.
     * Cette méthode supprime l'extension du fichier et normalise les caractères
     * spéciaux.
     *
     * @param filename Nom de fichier à normaliser
     * @return Nom de fichier normalisé sans extension
     */
    public static String normalizeLayerName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "layer_" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Supprimer l'extension du fichier (tout ce qui suit le dernier point)
        String nameWithoutExtension = filename;
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExtension = filename.substring(0, lastDotIndex);
        }

        // Normaliser le nom :
        // 1. Remplacer les caractères non alphanumériques par des underscores
        // 2. Supprimer les underscores multiples consécutifs
        // 3. Supprimer les underscores en début et fin de chaîne
        String normalized = nameWithoutExtension
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        // Si après normalisation le nom est vide, générer un nom par défaut
        if (normalized.isEmpty()) {
            normalized = "layer_" + UUID.randomUUID().toString().substring(0, 8);
        }

        return normalized;
    }

}
