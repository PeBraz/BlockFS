package com.blockfs.client;


import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.Block;
import com.blockfs.client.rest.model.PKBlock;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class BlockServerRequests implements IBlockServerRequests{


    public Block get(String id) throws ServerRespondedErrorException, IntegrityException {
        Block result = RestClient.GET(id);
        if(result.getType() == Block.PUBLIC){
            PKBlock pub = (PKBlock)result;
            String hash = "PK"+CryptoUtil.generateHash(pub.getPublicKey());
            if(!hash.equals(id))
                throw new IntegrityException("GET: Invalid public block received");

            if(!CryptoUtil.verifySignature(pub.getData(), pub.getSignature(), pub.getPublicKey())){
                throw new IntegrityException("GET: Public key block signature integrity failed");
            }
        }else if(result.getType() == Block.DATA){
            String hash = "DATA"+CryptoUtil.generateHash(result.getData());
            if(!hash.equals(id))
                throw new IntegrityException("GET: Invalid data block received");
        }

        return result;
    }

    public String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException, ServerRespondedErrorException {

        String pkHash = RestClient.POST_pkblock(data, signature, pubKey);

        if (!CryptoUtil.generateHash(pubKey).equals(pkHash))
            throw new IntegrityException("PUT_K: invalid public key hash received");

        return pkHash;
    }
    public String put_h(byte[] data) throws IntegrityException, ServerRespondedErrorException {

        String dataHash = RestClient.POST_cblock(data);

        if (!CryptoUtil.generateHash(data).equals(dataHash))
            throw new IntegrityException("PUT_H: invalid data hash received");

        return dataHash;
    }

    /**
     * Get certificates from server and return to client those that are correctly self-signed
     *
     */
    public List<PublicKey> readPubKeys() {

        List<X509Certificate> certificates = new ArrayList<>(); //TODO: get something from somewhere

        List<PublicKey> pbKeys = new ArrayList<>();
        for (X509Certificate cert: certificates) {
            try {
                cert.verify(cert.getPublicKey());
                pbKeys.add(cert.getPublicKey());
            } catch (CertificateException | SignatureException | NoSuchProviderException
                    | InvalidKeyException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return pbKeys;
    }

    //TODO: invalid certificate exception
    public void storePubKey(X509Certificate certificate) throws IntegrityException {
        //...
    }
}
