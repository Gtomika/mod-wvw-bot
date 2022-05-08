package com.gaspar.modwvwbot.exception;

/**
 * Exception when dps.report API does not respond correctly.
 */
public class DpsReportApiException extends RuntimeException {

    public DpsReportApiException(String message) {
        super(message);
    }
}
