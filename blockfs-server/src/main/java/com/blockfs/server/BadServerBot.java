package com.blockfs.server;


import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.BlockId;
import com.blockfs.server.rest.model.PKBlock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.util.Base64;

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
            System.out.println("returnResult:" + returnResult);
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

            System.out.println("returnResult:" + returnResult);
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
                System.out.println("returnResult WrongServerSignature2:" + returnResult);
                return returnResult;
            }

            returnResult = new String(dataBlock);
            System.out.println("returnResult WrongServerSignature:" + returnResult);
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


}
