package com.blockfs.client.rest;

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
import java.util.Base64;

public class RestClient {
    private static final String ENDPOINT = "http://0.0.0.0:4567/";
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static Gson GSON = new Gson();

    public static byte[] GET(String id){
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });
        GenericUrl url = new GenericUrl(ENDPOINT + "block/"+id);
        try {
            HttpRequest request = requestFactory.buildGetRequest(url);

            try{

                if(id.startsWith("PK")){

                    HttpResponse http = request.execute();

                    String result = new String(http.parseAsString().getBytes(), "ISO-8859-1");
                    PKBlock pkBlock = GSON.fromJson(result, PKBlock.class);

                    return pkBlock.getData();


                }else{
                    String json = request.execute().parseAsString();
                    return Base64.getDecoder().decode(json);
                }

            } catch (HttpResponseException e) {
                switch(e.getStatusCode()){
                    case 404:
                    case 400:
                        return new byte[0];
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }


    public static String POST_pkblock(byte[] data, byte[] signature, byte[] pubKey){
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
            e.printStackTrace();
        }

        return "";
    }



    public static String POST_cblock(byte[] data){
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
            e.printStackTrace();
        }

        return "";
    }
}
