package com.blockfs.client;


import com.blockfs.client.exception.InvalidCertificate;
import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.model.Block;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

public interface IBlockServerRequests {

    Block get(String hash) throws ServerRespondedErrorException, IntegrityException;
    String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException, ServerRespondedErrorException;
    String put_h(byte[] data) throws IntegrityException, ServerRespondedErrorException;

    void storePubKey(X509Certificate certificate) throws IntegrityException, ServerRespondedErrorException;
    List<PublicKey> readPubKeys() throws ServerRespondedErrorException, InvalidCertificate;

    class IntegrityException extends Exception {
        IntegrityException(String msg) {
            super(msg);
        }
    }
}
