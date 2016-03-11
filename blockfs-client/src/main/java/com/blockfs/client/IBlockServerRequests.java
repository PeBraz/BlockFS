package com.blockfs.client;


import com.blockfs.client.rest.model.Block;

public interface IBlockServerRequests {

    Block get(String hash) throws ServerRespondedErrorException, IntegrityException;
    String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException, ServerRespondedErrorException;
    String put_h(byte[] data) throws IntegrityException, ServerRespondedErrorException;

    class IntegrityException extends Exception {
        IntegrityException(String msg) {
            super(msg);
        }
    }
}
