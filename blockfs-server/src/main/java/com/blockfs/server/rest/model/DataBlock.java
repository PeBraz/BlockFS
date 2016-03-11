package com.blockfs.server.rest.model;

import java.util.Base64;

public class DataBlock {
    private String data;

    public DataBlock(byte[] data) {
        this.data = Base64.getEncoder().encodeToString(data);
    }

    public byte[] getData() {
        return Base64.getDecoder().decode(data);
    }

    public void setData(byte[] data) {
        this.data = Base64.getEncoder().encodeToString(data);
    }
}
