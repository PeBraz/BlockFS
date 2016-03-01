package com.blockfs.server;


import com.blockfs.server.exceptions.WrongDataSignature;

public interface IBlockServer {

    public byte[] get(String id);

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature;

    public String put_h(byte[] data);
}
