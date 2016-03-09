package com.blockfs.server;


import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.BlockId;
import com.blockfs.server.rest.model.DataBlock;
import com.blockfs.server.rest.model.PKBlock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.util.Base64;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

public class ComLine {



    public static void main(String[] args) {

            switch (args[0]) {
                case "-HELP": // help
                    break;
                case "-WCSIG": // wrong client signature
                    ComLine.routePutPKWrongClientSignature();
                    ComLine.routeGetBlock();
                    ComLine.routePutDB();
                    break;
                case "-WPHASH": // wrong put hash
                    ComLine.routePutDBWrongServerHash();
                    ComLine.routePutPKWrongServerHash();
                    ComLine.routeGetBlock();
                    break;
                case "-WSSIG": // wrong server signature
                    ComLine.routeGetPKWrongServerSignature();
                    ComLine.routePutDB();
                    ComLine.routeGetBlock();
                    break;
                case "-WGH": //wrong get hash (wether pkhash or content hash)
                    ComLine.routeGetWrongHash();
                    ComLine.routePutDB();
                    ComLine.routePutPK();
                    break;
                default:
                    System.err.println("Invalid option, use -HELP ");
                    return;
            }


    }

    public static Gson GSON = new Gson();
    public static BlockFSService BlockFSService = new BlockFSService();




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

    public static void routeGetWrongHash() {
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
            }else {
                DataBlock block = GSON.fromJson(new String(dataBlock), DataBlock.class);
                block.setData((new String(block.getData()) + "FAKE").getBytes()); // return wrong public key
                dataBlock = GSON.toJson(block).getBytes();
            }

            String returnResult = new String(dataBlock);
            System.out.println("returnResult:" + returnResult);
            return returnResult;
        });
    }

    public static void routeGetPKWrongServerSignature() {
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
            //Introduce fake data into the block, that won't correspond with signature
            if (id.startsWith("PK")) {
                PKBlock block = GSON.fromJson(new String(dataBlock), PKBlock.class);
                block.setData((new String(block.getData())+"FAKE").getBytes());
                dataBlock = GSON.toJson(block).getBytes();
            }

            String returnResult = new String(dataBlock);
            System.out.println("returnResult:" + returnResult);
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
