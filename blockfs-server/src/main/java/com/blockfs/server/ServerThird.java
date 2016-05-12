package com.blockfs.server;

import com.blockfs.server.exceptions.ReplayAttackException;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.BlockId;
import com.blockfs.server.rest.model.Certificate;
import com.blockfs.server.rest.model.PKBlock;
import com.blockfs.server.utils.CryptoUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.security.cert.X509Certificate;

import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.*;

public class ServerThird {

    private static BlockFSService BlockFSService = new BlockFSService();
    private static Gson GSON = new Gson();

    public ServerThird(int portSpark, String option){

        port(portSpark);
        BlockFSService.setPort(portSpark);


        switch (option) {
            case "timeout-pk":
                System.out.println("timeout-pk: Server will timeout on write pk block");
                BlockFSController.getBlock();

                timeout_pkblock();

                BlockFSController.cblock();

                BlockFSController.postCert();

                BlockFSController.getCert();
                break;
            case "timeout-cb":
                System.out.println("timeout-cb: Server will timeout on write data block");
                BlockFSController.getBlock();
                BlockFSController.postCert();
                BlockFSController.pkblock();
                BlockFSController.getCert();
                timeout_cblock();
                break;
            case "bad-hmac":
                System.out.println("timeout-cb: Server will reply with bad HMAC.");
                BlockFSController.getBlock();
                BlockFSController.postCert();
                BlockFSController.pkblock();
                BlockFSController.cblock();
                bad_hmac();
                break;

            case "out-order-pk":
                System.out.println("timeout-cb: Server will send an old PKBlock");
                BlockFSController.postCert();
                BlockFSController.getCert();
                BlockFSController.cblock();
                old_pkblock();
                break;


        }

    }

    public static void timeout_pkblock() {
        post("/pkblock", (request, response) -> {
            response.type("application/json");

            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            try {
                String id = BlockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

                BlockId blockId = new BlockId(id);
                System.out.println("pkblock saved:" + id);
                String json = GSON.toJson(blockId);

                try{
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

    public static void timeout_cblock() {

        post("/cblock", (request, response) -> {
            response.type("application/json");

            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());
            String id = BlockFSService.put_h(data);

            BlockId blockId = new BlockId(id);
            try{
                System.out.println("start timeout");
                Thread.sleep(15000);
                System.out.println("end timeout");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return GSON.toJson(blockId);

        });

    }

    public static void bad_hmac() {

        get("/cert", (request, response) -> {
            response.type("application/json");

            response.header("Authorization", BlockFSController.buildHMAC(request, "secret", 1));

            List<Certificate> certificateList = new LinkedList<Certificate>();

            for(X509Certificate cert : BlockFSService.readPubKeys()) {
                certificateList.add(new Certificate(cert.getSubjectDN().getName(), cert.getEncoded()));
            }

            return GSON.toJson(certificateList);
        });
    }

    static String dataOld = null;
    public static void old_pkblock(){

            get("/block/:id", (request, response) -> {
                response.type("application/json");

                String returnResult;
                String id = request.params(":id");
                System.out.println("GET block:"+id);

                byte[] dataBlock = new byte[0];
                try {
                    dataBlock = BlockFSService.get(id);
                } catch (FileNotFoundException e) {
                    System.out.println("File with id < " + id + " > not found");
                    halt(404);
                }

                if(id.startsWith("DATA")) {
                    returnResult = Base64.getEncoder().encodeToString(dataBlock);
                }else {
                    String json = dataOld;
                    String randomId = request.headers("sessionid");
                    String hash = CryptoUtil.generateHash((json + randomId).getBytes());
                    response.header("sessionid", hash);

                    returnResult = json;
                }

                return returnResult;
            });


        post("/pkblock", (request, response) -> {
            response.type("application/json");


            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            try {
                String id = BlockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

                BlockId blockId = new BlockId(id);
                System.out.println("pkblock saved:" + id);
                String json = GSON.toJson(blockId);

                if(dataOld == null){
                    System.out.println("dataOld is null:");
                    dataOld  = GSON.toJson(pkBlock, PKBlock.class);
                }

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

}
