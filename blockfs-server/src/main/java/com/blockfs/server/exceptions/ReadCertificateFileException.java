package com.blockfs.server.exceptions;

public class ReadCertificateFileException extends Exception{

    private static final long serialVersionUID = 1L;

    public ReadCertificateFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadCertificateFileException(String message) {
        super(message);
    }
}
