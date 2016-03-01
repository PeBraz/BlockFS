package com.blockfs.client;


import java.util.List;

public interface IBlockServerRequests {

    byte[] get(String hash);
    String put_k(byte[] data, String signature, String pubKey);
    String put_h(byte[] data);

    List<String> getPKB(String hash);
    void putPKB(List<String> hashes, String signature, String pubKey);
}
