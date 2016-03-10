package com.blockfs.client.rest.model;


public abstract class Block {
    public static final int PUBLIC = 0;
    public static final int DATA = 1;

    public abstract byte[] getData();
    public abstract int getType();
}
