package com.blockfs.client;



public interface IBlockServerRequests {

    byte[] get(String hash);
    String put_k(byte[] data, String signature, String pubKey) throws IntegrityException;
    String put_h(byte[] data) throws IntegrityException;

    class IntegrityException extends Exception {}
}
