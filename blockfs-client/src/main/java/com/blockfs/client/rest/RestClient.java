package com.blockfs.client.rest;

import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.model.Block;
import com.blockfs.client.rest.model.BlockId;
import com.blockfs.client.rest.model.DataBlock;
import com.blockfs.client.rest.model.PKBlock;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class RestClient {
    private static final String ENDPOINT = "http://0.0.0.0:4567/";
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static Gson GSON = new Gson();

    public static Block GET(String id) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });

        GenericUrl url = new GenericUrl(ENDPOINT + "block/"+id);

        try{
            HttpRequest request = requestFactory.buildGetRequest(url);
            if(id.startsWith("PK")){

                HttpResponse http = request.execute();

                String result = new String(http.parseAsString().getBytes(), "ISO-8859-1");
                PKBlock pkBlock = GSON.fromJson(result, PKBlock.class);

                return pkBlock;

            }else{
                String json = request.execute().parseAsString();
                DataBlock db = new DataBlock(json);
                return db;
            }

        } catch (IOException e) {
            throw new ServerRespondedErrorException();
        }

    }


    public static String POST_pkblock(byte[] data, byte[] signature, byte[] pubKey) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });

        GenericUrl url = new GenericUrl(ENDPOINT + "pkblock");

        try {

            PKBlock pkBlock = new PKBlock(data, signature, pubKey);
            String requestBody = GSON.toJson(pkBlock);
            HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestBody ));
            String json = request.execute().parseAsString();
            BlockId blockId = GSON.fromJson(json, BlockId.class);
            return blockId.getId().substring(2);

        } catch (IOException e) { // HTTPResponseException 400
            throw new ServerRespondedErrorException();
        }
    }



    public static String POST_cblock(byte[] data) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });

        GenericUrl url = new GenericUrl(ENDPOINT + "cblock");

        try {

            DataBlock dataBlock = new DataBlock(data);
            String requestBody = GSON.toJson(dataBlock);
            HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestBody ));
            String json = request.execute().parseAsString();
            BlockId blockId = GSON.fromJson(json, BlockId.class);

            return blockId.getId().substring(4); //Remove DATA from string

        } catch (IOException e) {
            throw new ServerRespondedErrorException();
        }
    }

    public static void POST_cert(X509Certificate certificate) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

        GenericUrl url = new GenericUrl(ENDPOINT + "cert");

        try {
            byte[] data = certificate.getEncoded();
            String requestBody = Base64.getEncoder().encodeToString(data);

            HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestBody ));
            //String json = request.execute().parseAsString();

        } catch (IOException e) {
            throw new ServerRespondedErrorException();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
    }

}
