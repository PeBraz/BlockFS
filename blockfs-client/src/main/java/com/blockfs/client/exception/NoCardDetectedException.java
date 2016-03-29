package com.blockfs.client.exception;

public class NoCardDetectedException extends Exception {

    public NoCardDetectedException() {
    }

    public NoCardDetectedException(String message) {
        super(message);
    }

    public NoCardDetectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoCardDetectedException(Throwable cause) {
        super(cause);
    }

    public NoCardDetectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
