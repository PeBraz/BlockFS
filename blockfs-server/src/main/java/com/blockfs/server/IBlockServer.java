package com.ist.blockfs.server;


public interface IBlockServer {

    public byte[] get(String id);

    public String put_k(byte[] data, String signature, String publicKey);

    public String put_h(byte[] data);
}
