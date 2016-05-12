package com.blockfs.client.rest;

import com.blockfs.client.CCBlockClient;
import com.blockfs.client.Config;
import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.model.*;
import com.blockfs.client.util.CryptoUtil;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RestClient {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    static final int TIMEOUT = 10000;
    private static Gson GSON = new Gson();

    public static Block GET(String id, String ENDPOINT) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                    request.setConnectTimeout(TIMEOUT);
                    request.setReadTimeout(TIMEOUT);
                }
            });

        GenericUrl url = new GenericUrl(ENDPOINT + "block/"+id);
        Random randomGenerator = new Random();
        int randomId = randomGenerator.nextInt();
        try{
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpHeaders header = new HttpHeaders();

            if(id.startsWith("PK")) {
                header.put("sessionId", randomId);
            }

            //Build HMAC and append it
            String hmac = buildHMAC(request, Config.ENDPOINTS.get(ENDPOINT));
            header.setAuthorization(hmac);

            request.setHeaders(header);

            HttpResponse response = request.execute();
            String json = response.parseAsString();

            if(!verifyHMAC(request, Config.ENDPOINTS.get(ENDPOINT), response.getHeaders().getAuthorization())) {
                throw new ServerRespondedErrorException("Bad HMAC");
            }

            if(id.startsWith("PK")){
                String serverResponseHash;

                if(!response.getHeaders().containsKey("sessionid"))
                    serverResponseHash = "";
                else {
                    serverResponseHash = response.getHeaders().getFirstHeaderStringValue("sessionid");
                }

                String hash = CryptoUtil.generateHash((json + randomId).getBytes());

                if(!hash.equals(serverResponseHash)) {
                    throw new ServerRespondedErrorException("replay attack");
                }
                PKBlock pkBlock = GSON.fromJson(json, PKBlock.class);

                return pkBlock;

            }else{
                DataBlock db = new DataBlock(json);
                return db;
            }

        } catch (IOException e) {
            if(e.getMessage().startsWith("404"))
                throw new ServerRespondedErrorException("404");
            else
                throw new ServerRespondedErrorException();
        }

    }


    public static String POST_pkblock(byte[] data, byte[] signature, byte[] pubKey, String ENDPOINT) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {

                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                    request.setConnectTimeout(TIMEOUT);
                    request.setReadTimeout(TIMEOUT);
                }
            });

        GenericUrl url = new GenericUrl(ENDPOINT + "pkblock");

        try {

            PKBlock pkBlock = new PKBlock(data, signature, pubKey);
            String requestBody = GSON.toJson(pkBlock);

            HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestBody));
            HttpHeaders headers = new HttpHeaders();

            //Build HMAC and append it
            String hmac = buildHMAC(request, Config.ENDPOINTS.get(ENDPOINT));
            headers.setAuthorization(hmac);
            request.setHeaders(headers);

            HttpResponse response = request.execute();

            if(!verifyHMAC(request, Config.ENDPOINTS.get(ENDPOINT), response.getHeaders().getAuthorization())) {
                throw new ServerRespondedErrorException("Bad HMAC");
            }

            String json = response.parseAsString();
            BlockId blockId = GSON.fromJson(json, BlockId.class);

            return blockId.getId().substring(2);
        } catch (IOException e) { // HTTPResponseException 400
            System.out.println("***"+e.getMessage());

            if(e.getMessage().startsWith("Read timed out"))
                throw new ServerRespondedErrorException("Read timed out");
            else if(e.getMessage().startsWith("401")){
                throw new ServerRespondedErrorException("401");
            }else
                throw new ServerRespondedErrorException();
        }
    }



    public static String POST_cblock(byte[] data, String ENDPOINT) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {

                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                    request.setConnectTimeout(TIMEOUT);
                    request.setReadTimeout(TIMEOUT);
                }
            });

        GenericUrl url = new GenericUrl(ENDPOINT + "cblock");

        try {

            DataBlock dataBlock = new DataBlock(data);
            String requestBody = GSON.toJson(dataBlock);
            HttpHeaders headers = new HttpHeaders();
            HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestBody));

            //Build HMAC and append it
            String hmac = buildHMAC(request, Config.ENDPOINTS.get(ENDPOINT));
            headers.setAuthorization(hmac);
            request.setHeaders(headers);

            HttpResponse response = request.execute();

            if(!verifyHMAC(request, Config.ENDPOINTS.get(ENDPOINT), response.getHeaders().getAuthorization())) {
                throw new ServerRespondedErrorException("Bad HMAC");
            }

            String json = response.parseAsString();
            BlockId blockId = GSON.fromJson(json, BlockId.class);

            return blockId.getId().substring(4); //Remove DATA from string

        } catch (IOException e) {
            throw new ServerRespondedErrorException();
        }
    }

    public static void POST_certificate(X509Certificate certificate, int version, String ENDPOINT) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                        request.setConnectTimeout(TIMEOUT);
                        request.setReadTimeout(TIMEOUT);
                    }
                });

        GenericUrl url = new GenericUrl(ENDPOINT + "cert");

        try {
            Certificate cert = new Certificate(certificate.getSubjectDN().getName(), certificate.getEncoded());

            String requestBody = GSON.toJson(cert);
            HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestBody ));
            addVersionHeader(request, version);

            //Build HMAC and append it
            String hmac = buildHMAC(request, Config.ENDPOINTS.get(ENDPOINT));
            request.getHeaders().setAuthorization(hmac);

            HttpResponse response = request.execute();

            if(!verifyHMAC(request, Config.ENDPOINTS.get(ENDPOINT), response.getHeaders().getAuthorization())) {
                throw new ServerRespondedErrorException("Bad HMAC");
            }


        } catch (IOException e) {
            throw new ServerRespondedErrorException();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
    }

    public static List<X509Certificate> GET_certificates(String ENDPOINT) throws ServerRespondedErrorException {
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                        request.setConnectTimeout(TIMEOUT);
                        request.setReadTimeout(TIMEOUT);
                    }
                });

        GenericUrl url = new GenericUrl(ENDPOINT + "cert");

        try{
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpHeaders headers = new HttpHeaders();

            //Build HMAC and append it
            String hmac = buildHMAC(request, Config.ENDPOINTS.get(ENDPOINT));
            headers.setAuthorization(hmac);

            request.setHeaders(headers);

            HttpResponse response = request.execute();

            if(!verifyHMAC(request, Config.ENDPOINTS.get(ENDPOINT), response.getHeaders().getAuthorization())) {
                throw new ServerRespondedErrorException("Bad HMAC");
            }

            String json = new String(response.parseAsString().getBytes(), "ISO-8859-1");
            List<Certificate> certificatesTransferList = GSON.fromJson(json, new TypeToken<List<Certificate>>(){}.getType());
            byte[] cert;
            CertificateFactory certificateFactory;
            InputStream in;
            List<X509Certificate> certificatesX509 = new ArrayList<>();
            for (Certificate c : certificatesTransferList){
                cert = c.getCertificate();
                certificateFactory = CertificateFactory.getInstance("X.509");
                in = new ByteArrayInputStream(cert);
                certificatesX509.add((X509Certificate)certificateFactory.generateCertificate(in));
            }

            return certificatesX509;


        } catch (IOException | CertificateException e) {
            e.printStackTrace();
            throw new ServerRespondedErrorException();
        }
    }

    public static void addVersionHeader(HttpRequest request, int version){
        HttpHeaders header = new HttpHeaders();
        header.put("version", version == CCBlockClient.VERSION_WITH_CARD ? "V2" : "V1");
        request.setHeaders(header);
    }

    public static String buildHMAC(HttpRequest request, String secret) {

        HttpHeaders headers = request.getHeaders();
        List<String> fields = new LinkedList<>();

        fields.add(request.getRequestMethod());
        fields.add(headers.getContentType());
        fields.add(request.getUrl().getRawPath());

        String message = fields.stream().collect(Collectors.joining(""));

        try {
            return CryptoUtil.calculateHMAC(message, secret);
        } catch (SignatureException e) {
            return null;
        }
    }

    public static boolean verifyHMAC(HttpRequest request, String secret, String extracted) {

        HttpHeaders headers = request.getHeaders();
        List<String> fields = new LinkedList<>();

        fields.add(request.getRequestMethod());
        fields.add(headers.getContentType());
        fields.add(request.getUrl().getRawPath());

        String message = fields.stream().collect(Collectors.joining("")) + "RESPONSE";

        try {
            return CryptoUtil.verifyHMAC(message, secret, extracted);
        } catch (SignatureException e) {
            return false;
        }

    }


}

