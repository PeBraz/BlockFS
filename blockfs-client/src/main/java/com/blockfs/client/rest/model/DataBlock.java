package com.blockfs.client.rest.model;

import java.util.Base64;

public class DataBlock extends Block {
    private String data;

    public DataBlock(String data) {
        this.data = data;
    }

    public DataBlock(byte[] data) {
        this.data = Base64.getEncoder().encodeToString(data);
    }

    public byte[] getData() {

        return Base64.getDecoder().decode(data);
    }

    @Override
    public int getType() {
        return Block.DATA;
    }

    public void setData(byte[] data) {
        this.data = Base64.getEncoder().encodeToString(data);
    }
}
