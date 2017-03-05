package com.tytanapps.ptsd;

/**
 * Created by tyler on 3/4/17.
 */

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException() {
    }

    public LocationNotFoundException(String message) {
        super(message);
    }
}
