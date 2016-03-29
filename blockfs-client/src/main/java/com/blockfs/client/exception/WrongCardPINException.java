package com.blockfs.client.exception;

public class WrongCardPINException extends Exception {

    public WrongCardPINException() {
    }

    public WrongCardPINException(String message) {
        super(message);
    }

    public WrongCardPINException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongCardPINException(Throwable cause) {
        super(cause);
    }

    public WrongCardPINException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
