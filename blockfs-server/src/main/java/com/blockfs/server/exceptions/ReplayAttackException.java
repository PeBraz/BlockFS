package com.blockfs.server.exceptions;

public class ReplayAttackException extends Exception {

    public ReplayAttackException() {
    }

    public ReplayAttackException(String message) {
        super(message);
    }

    public ReplayAttackException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReplayAttackException(Throwable cause) {
        super(cause);
    }

    public ReplayAttackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
