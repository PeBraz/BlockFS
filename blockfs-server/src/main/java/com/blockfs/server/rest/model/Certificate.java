package com.blockfs.server.rest.model;

import java.util.Base64;

public class Certificate {

    private String subject;
    private String publicKey;

    public Certificate() {
    }

    public Certificate(String subject, byte[] publicKey) {
        this.subject = subject;
        this.publicKey = Base64.getEncoder().encodeToString(publicKey);
    }

    public byte[] getPublicKey() {
        return Base64.getDecoder().decode(publicKey);
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = Base64.getEncoder().encodeToString(publicKey);
    }

}
