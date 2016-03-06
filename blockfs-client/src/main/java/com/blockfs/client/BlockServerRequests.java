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

        byte[] result = RestClient.GET(id);


        return result;
    }

    public String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException {
        //TODO Integrity Check (hash(pubkey) == id)

        String result = RestClient.POST_pkblock(data, signature, pubKey);

        return result;
    }
    public String put_h(byte[] data) throws IntegrityException{
        //TODO Integrity Check (hash(data) == id)
        String result = RestClient.POST_cblock(data);
        return result;
    }


}
