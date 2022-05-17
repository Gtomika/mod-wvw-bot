package com.gaspar.modwvwbot.exception;

public class Gw2ApiException extends RuntimeException {

    public Gw2ApiException(String message) {
        super(message);
    }

    public Gw2ApiException(Throwable cause) {
        super(cause);
    }
}
