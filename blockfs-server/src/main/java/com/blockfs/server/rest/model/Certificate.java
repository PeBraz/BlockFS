package com.blockfs.server.rest.model;

import java.util.Base64;

public class Certificate {

    private String subject;
    private String certificate;

    public Certificate() {
    }

    public Certificate(String subject, byte[] certificate) {
        this.subject = subject;
        this.certificate = Base64.getEncoder().encodeToString(certificate);
    }

    public byte[] getCertificate() {
        return Base64.getDecoder().decode(certificate);
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = Base64.getEncoder().encodeToString(certificate);
    }

}
