package com.blockfs.server.exceptions;

public class WrongDataSignature extends Exception {

    public WrongDataSignature() {
    }

    public WrongDataSignature(String message) {
        super(message);
    }

    public WrongDataSignature(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongDataSignature(Throwable cause) {
        super(cause);
    }

    public WrongDataSignature(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
