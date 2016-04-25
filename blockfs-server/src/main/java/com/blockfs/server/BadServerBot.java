package com.blockfs.server;


import com.blockfs.server.exceptions.InvalidCertificate;
import com.blockfs.server.exceptions.ReplayAttackException;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.BlockId;
import com.blockfs.server.rest.model.Certificate;
import com.blockfs.server.rest.model.PKBlock;
import com.blockfs.server.utils.CryptoUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.*;

public class BadServerBot {



    public static Gson GSON = new Gson();
    public static BlockFSService BlockFSService = new BlockFSService();


    public static void main(String[] args) {
            if (args.length != 1) return;

            System.out.println("Started with option: "+ args[0]);

            switch (args[0]) {
                case "-HELP": // help
                    System.out.println("-WCSIG: Server will return 400, as if client had wrong client signature on put_k");
                    System.out.println("-WPHASH: Server will return wrong hash on put_k/put_h");
                    System.out.println("-WSSIG: Server will change data on get, making the signature invalid at the client");
                    System.out.println("-WGPKHASH: Server will return wrong public key (pk block) on get");
                    System.out.println("-WGDBHASH: Server will return wrong data block contents on get");
                    System.out.println("-WGLISTCERT: Server will return certificate list");
                    System.out.println("-WGREPLAYATTACK: Simulates a replay attack to the client");
                    break;
                case "-WCSIG": // wrong client signature
                    BadServerBot.wrongClientSignature();
                    break;
                case "-WPHASH": // wrong put hash
                    BadServerBot.wrongPutHash();
                    break;
                case "-WSSIG": // wrong server signature
                    BadServerBot.wrongServerSignature();
                    break;
                case "-WGPKHASH": //wrong get pk hash
                    BadServerBot.wrongGetPKHash();
                    break;
                case "-WGDBHASH": // wrong get data block hash
                    BadServerBot.wrongGetDBHash();
                    break;
                case "-WGLISTCERT":
                    BadServerBot.wrongGetListKeys();
                    break;
                case "-WGREPLAYATTACK":
                    BadServerBot.wrongGetWithReplayAttack();
                    break;


                default:
                    System.err.println("Invalid option, use -HELP ");
            }


    }


    public static void wrongClientSignature() {
        BadServerBot.routePutPKWrongClientSignature();
        BadServerBot.routeGetBlock();
        BadServerBot.routePutDB();
    }
    public static void wrongPutHash() {
        BadServerBot.routePutDBWrongServerHash();
        BadServerBot.routePutPKWrongServerHash();
        BadServerBot.routeGetBlock();
    }
    public static void wrongServerSignature() {
        BadServerBot.routeGetPKWrongServerSignature();
        BadServerBot.routePutDB();
        BadServerBot.routePutPK();
    }
    public static void wrongGetPKHash() {
        BadServerBot.routeGetWrongPKHash();
        BadServerBot.routePutDB();
        BadServerBot.routePutPK();
    }

    public static void wrongGetDBHash() {
        BadServerBot.routeGetWrongDBHash();
        BadServerBot.routePutDB();
        BadServerBot.routePutPK();
    }

    public static void wrongGetListKeys() {
        BadServerBot.routePostPKBlock();
        BadServerBot.routeGetBlockNew();
        BadServerBot.routePutDB();
        BadServerBot.routePostCertificate();
        BadServerBot.routeWrongGETCertificates();
    }

    public static void wrongGetWithReplayAttack() {
        BadServerBot.routePostPKBlock();
        BadServerBot.routeGetBlockReplayAttack();
        BadServerBot.routePutDB();
        BadServerBot.routePostCertificate();
        BadServerBot.routeGETCertificates();
    }


    public static void routePutPKWrongClientSignature () {
        post("/pkblock", (request, response) -> {
            response.type("application/json");
            halt(400);
            return "";
        });
    }

    public static void routePutPKWrongServerHash() {
        post("/pkblock", (request, response) -> {
            response.type("application/json");

            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            try {
                String id = BlockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

                BlockId blockId = new BlockId(id + "FAKE");
                System.out.println("pkblock saved:" + id);
                return GSON.toJson(blockId);
            }catch (WrongDataSignature e) {
                halt(400);
                return "";
            }
        });
    }



    public static void routePutDBWrongServerHash() {
        post("/cblock", (request, response) -> {
            response.type("application/json");

            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());
            String id = BlockFSService.put_h(data);

            BlockId blockId = new BlockId(id + "FAKE");

            return GSON.toJson(blockId);

        });
    }

    public static void routeGetBlock() {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String id = request.params(":id");
            System.out.println("GET block:" + id);

            byte[] dataBlock = new byte[0];
            try {
                dataBlock = BlockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }


            String returnResult = new String(dataBlock);
            return returnResult;
        });
    }

    public static void routeGetWrongPKHash() {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String id = request.params(":id");
            System.out.println("GET block:" + id);

            byte[] dataBlock = new byte[0];
            try {
                dataBlock = BlockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }

            if (id.startsWith("PK")) {
                PKBlock block = GSON.fromJson(new String(dataBlock), PKBlock.class);
                block.setPublicKey((new String(block.getPublicKey()) + "FAKE").getBytes()); // return wrong public key
                dataBlock = GSON.toJson(block).getBytes();
            }

            String returnResult = new String(dataBlock);
            System.out.println("returnResult:" + returnResult);
            return returnResult;
        });
    }

    public static void routeGetWrongDBHash() {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String id = request.params(":id");
            System.out.println("GET block:" + id);

            byte[] dataBlock = new byte[0];
            try {
                dataBlock = BlockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }
            String returnResult;
            if(id.startsWith("DATA")) {
                returnResult = Base64.getEncoder().encodeToString(dataBlock);
            }else {
                returnResult = new String(dataBlock);
            }
            if (id.startsWith("DATA")) {
                returnResult = "FAKE" + returnResult;
            }

            return returnResult;
        });
    }

    public static void routeGetPKWrongServerSignature() {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String id = request.params(":id");
            System.out.println("GET block:" + id);
            String returnResult;
            byte[] dataBlock = new byte[0];
            try {
                dataBlock = BlockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }
            //Introduce fake data into the block, that won't correspond with signature
            if (id.startsWith("PK")) {
                PKBlock block = GSON.fromJson(new String(dataBlock), PKBlock.class);
                block.setData( ("FAKE" + ( new String( block.getData()))).getBytes());


                returnResult = GSON.toJson(block);
                return returnResult;
            }

            returnResult = new String(dataBlock);
            return returnResult;
        });
    }

    public static void routePutPK() {
        post("/pkblock", (request, response) -> {
            response.type("application/json");

            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            try {
                String id = BlockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

                BlockId blockId = new BlockId(id);
                System.out.println("pkblock saved:" + id);
                return GSON.toJson(blockId);
            } catch (WrongDataSignature e) {
                halt(400);
                return "";
            }

        });
    }

    public static void routePutDB() {
        post("/cblock", (request, response) -> {
            response.type("application/json");

            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());
            String id = BlockFSService.put_h(data);

            BlockId blockId = new BlockId(id);

            return GSON.toJson(blockId);

        });
    }

    public static void routeGETCertificates () {
        get("/cert", (request, response) -> {
            response.type("application/json");
            System.out.println("cert GET:");


            List<Certificate> certificateList = new LinkedList<Certificate>();

            for (X509Certificate cert : BlockFSService.readPubKeys()) {
                certificateList.add(new Certificate(cert.getSubjectDN().getName(), cert.getEncoded()));
            }
            return GSON.toJson(certificateList);
        });
    }

    public static void routePostPKBlock () {
        post("/pkblock", (request, response) -> {
            response.type("application/json");

            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            try {
                String id = BlockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

                BlockId blockId = new BlockId(id);
                System.out.println("pkblock saved:" + id);
                String json = GSON.toJson(blockId);


                return json;
            }catch (WrongDataSignature e) {
                halt(400);
                return "";
            }catch (ReplayAttackException e){
                halt(401);
                return "";
            }

        });
    }

    public static void routePostCertificate () {
        post("/cert", (request, response) -> {
            response.type("application/json");
            System.out.println("cert POst:");
            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();
            byte[] certificate = Base64.getDecoder().decode(body.get("certificate").getAsString());

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(certificate);

            X509Certificate cert = (X509Certificate)certificateFactory.generateCertificate(in);

            try {
                BlockFSService.storePubKey(cert, true);
            }catch(InvalidCertificate e) {
                e.printStackTrace();
                halt(400);
                return "Invalid certificate.";
            }

            return "Certificate saved.";

        });
    }

    public static void routeGetBlockNew () {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String returnResult;
            String id = request.params(":id");
            System.out.println("GET block:" + id);

            byte[] dataBlock = new byte[0];
            try {
                dataBlock = BlockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }

            if (id.startsWith("DATA")) {
                returnResult = Base64.getEncoder().encodeToString(dataBlock);
            } else {
                String json = new String(dataBlock);
                String randomId = request.headers("sessionid");
                String hash = CryptoUtil.generateHash((json + randomId).getBytes());
                response.header("sessionid", hash);

                returnResult = json;
            }

            return returnResult;
        });
    }


    public static void routeWrongGETCertificates () {
        get("/cert", (request, response) -> {
            response.type("application/json");
            System.out.println("cert GET:");
            List<Certificate> certificateList = new LinkedList<Certificate>();
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keys = keygen.generateKeyPair();
            X509Certificate cert = CryptoUtil.generateCertificate(keys);
            certificateList.add(new Certificate(cert.getSubjectDN().getName(), cert.getEncoded()));

            return GSON.toJson(certificateList);
        });
    }


    public static void routeGetBlockReplayAttack () {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String returnResult;
            String id = request.params(":id");
            System.out.println("GET block:" + id);

            byte[] dataBlock = new byte[0];
            try {
                dataBlock = BlockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }

            if (id.startsWith("DATA")) {
                returnResult = Base64.getEncoder().encodeToString(dataBlock);
            } else {
                String json = new String(dataBlock);
                //different id from the expected by client
                String randomId = "999";
                System.out.println("chegou get PK");
                String hash = CryptoUtil.generateHash((json + randomId).getBytes());
                response.header("sessionid", hash);

                returnResult = json;
            }

            return returnResult;
        });
    }




}
