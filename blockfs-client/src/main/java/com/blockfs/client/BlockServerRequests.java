package com.blockfs.client;


import com.blockfs.client.rest.RestClient;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class BlockServerRequests implements IBlockServerRequests{

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();


    public byte[] get(String id) {
        //TODO Block discrimination + integrity (verify signature)
        byte[] result = RestClient.GET(id);

        return result;
    }

    public String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException {

        String pkHash = RestClient.POST_pkblock(data, signature, pubKey);

        if (!CryptoUtil.generateHash(pubKey).equals(pkHash))
            throw new IntegrityException("PUT_K: invalid public key hash received");

        return pkHash;
    }
    public String put_h(byte[] data) throws IntegrityException{

        String dataHash = RestClient.POST_cblock(data);

        if (!CryptoUtil.generateHash(data).equals(dataHash))
            throw new IntegrityException("PUT_H: invalid data hash received");

        return dataHash;
    }


}
