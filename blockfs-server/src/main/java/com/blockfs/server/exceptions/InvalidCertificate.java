package com.blockfs.server.exceptions;

public class InvalidCertificate extends Exception{

    private static final long serialVersionUID = 1L;

    public InvalidCertificate(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCertificate(String message) {
        super(message);
    }
}
