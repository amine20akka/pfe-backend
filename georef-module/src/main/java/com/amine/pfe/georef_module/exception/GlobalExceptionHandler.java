package com.amine.pfe.georef_module.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.amine.pfe.georef_module.gcp.exceptions.DuplicateGcpIndexException;
import com.amine.pfe.georef_module.image.exceptions.UnsupportedImageFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedImageFormatException.class)
    public ResponseEntity<String> handleUnsupportedImageFormatException(UnsupportedImageFormatException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                             .body("Unsupported image format: " + ex.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<String> handleImageNotFoundException(ImageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Image not found: " + ex.getMessage());
    }

    @ExceptionHandler(DuplicateGcpIndexException.class)
    public ResponseEntity<String> handleDuplicateGcpIndexException(DuplicateGcpIndexException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body("Duplicate GCP index: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("An error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(NotEnoughGcpException.class)
    public ResponseEntity<String> handleNotEnoughGcpException(NotEnoughGcpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("Not enough GCPs: " + ex.getMessage());
    }
}

