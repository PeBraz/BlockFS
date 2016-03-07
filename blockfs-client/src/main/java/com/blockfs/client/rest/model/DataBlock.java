package com.blockfs.client.rest.model;

import java.util.Base64;

/**
 * Created by joaosampaio on 06-03-2016.
 */
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
