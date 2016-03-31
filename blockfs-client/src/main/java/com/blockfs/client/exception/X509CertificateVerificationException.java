package com.blockfs.client.exception;

public class X509CertificateVerificationException extends Exception{

    private static final long serialVersionUID = 1L;

    public X509CertificateVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public X509CertificateVerificationException(String message) {
        super(message);
    }
}
