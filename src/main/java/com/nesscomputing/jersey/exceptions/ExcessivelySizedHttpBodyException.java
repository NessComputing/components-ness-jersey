package com.nesscomputing.jersey.exceptions;

import java.io.IOException;

/**
 * Thrown when the service is administratively configured to reject
 * excessively large POST/PUT bodies and that limit is exceeded.
 */
public class ExcessivelySizedHttpBodyException extends IOException {
    private static final long serialVersionUID = 1L;

    public ExcessivelySizedHttpBodyException(String message) {
        super(message);
    }
}
