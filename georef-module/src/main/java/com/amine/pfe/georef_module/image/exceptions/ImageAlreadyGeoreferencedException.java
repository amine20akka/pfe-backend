package com.amine.pfe.georef_module.image.exceptions;

public class ImageAlreadyGeoreferencedException extends RuntimeException {
    
    public ImageAlreadyGeoreferencedException(String message) {
        super(message);
    }
    
    public ImageAlreadyGeoreferencedException(String message, Throwable cause) {
        super(message, cause);
    }
}
