package com.blockfs.client;


import com.blockfs.client.rest.model.Block;

public interface IBlockServerRequests {

    Block get(String hash) throws ServerRespondedErrorException;
    String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException;
    String put_h(byte[] data) throws IntegrityException;

    class IntegrityException extends Exception {
        IntegrityException(String msg) {
            super(msg);
        }
    }
}
