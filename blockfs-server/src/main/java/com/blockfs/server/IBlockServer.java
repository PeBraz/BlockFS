package com.blockfs.server;


import com.blockfs.server.exceptions.WrongDataSignature;

import java.io.FileNotFoundException;

public interface IBlockServer {

    public byte[] get(String id) throws FileNotFoundException, WrongDataSignature;

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature;

    public String put_h(byte[] data);
}
