package com.blockfs.client.exception;

public class ClientProblemException extends Exception {

    public ClientProblemException() {
    }

    public ClientProblemException(String message) {
        super(message);
    }

    public ClientProblemException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientProblemException(Throwable cause) {
        super(cause);
    }

    public ClientProblemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
