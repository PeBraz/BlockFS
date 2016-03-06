package com.blockfs.server.rest.model;

import java.util.Base64;

/**
 * Created by joaosampaio on 06-03-2016.
 */
public class PKBlock {

    private String signature;
    private String publicKey;
    private String data;

    public PKBlock() {
    }

    public PKBlock(byte[] data, byte[] signature, byte[] publicKey) {


        this.data = Base64.getEncoder().encodeToString(data);
        this.signature = Base64.getEncoder().encodeToString(signature);
        this.publicKey = Base64.getEncoder().encodeToString(publicKey);
    }

    public byte[] getSignature() {
        return Base64.getDecoder().decode(signature);
    }

    public void setSignature(byte[] signature) {
        this.signature = Base64.getEncoder().encodeToString(signature);
    }

    public byte[] getPublicKey() {
        return Base64.getDecoder().decode(publicKey);
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = Base64.getEncoder().encodeToString(publicKey);
    }

    public byte[] getData() {
        return Base64.getDecoder().decode(data);
    }

    public void setData(byte[] data) {
        this.data = Base64.getEncoder().encodeToString(data);
    }
}
