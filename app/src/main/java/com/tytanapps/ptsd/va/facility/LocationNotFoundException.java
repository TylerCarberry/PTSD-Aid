package com.tytanapps.ptsd.va.facility;

/**
 * The user's location can not be determined
 */
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException() {
    }

    public LocationNotFoundException(String message) {
        super(message);
    }
}
