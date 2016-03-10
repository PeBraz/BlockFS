package com.blockfs.client.rest.model;

import com.google.api.client.util.Key;

import java.util.Base64;

public class PKBlock extends Block {
    @Key
    private String signature;

    @Key
    private String publicKey;

    @Key
    private String data;

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

    @Override
    public int getType() {
        return Block.PUBLIC;
    }
}
