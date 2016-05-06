package com.blockfs.client.exception;

public class ValidationException extends Exception{

    private static final long serialVersionUID = 1L;

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException() {}
}
