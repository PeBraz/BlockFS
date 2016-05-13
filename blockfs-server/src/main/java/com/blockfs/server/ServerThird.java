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
import spark.Request;

import java.security.SignatureException;
import java.security.cert.X509Certificate;

import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.blockfs.server.BlockFSController.verifyHMAC;
import static spark.Spark.*;

public class ServerThird {

    private static BlockFSService blockFSService = new BlockFSService();
    private static BlockFSController controller;
    private static Gson GSON = new Gson();
    private static final String SECRET = "secret";
    private static int portS;

    public ServerThird(int portSpark, String option){

        port(portSpark);
        blockFSService.setPort(portSpark);
        portS = portSpark;
        BlockFSController.portSpark = portSpark;
        controller = new BlockFSController(portSpark);
        switch (option) {
            case "timeout-pk":
                System.out.println("timeout-pk: Server will timeout on write pk block");
                controller.getBlock();

                timeout_pkblock();

                controller.cblock();

                controller.postCert();

                controller.getCert();
                break;
            case "timeout-cb":
                System.out.println("timeout-cb: Server will timeout on write data block");
                controller.getBlock();
                controller.postCert();
                controller.pkblock();
                controller.getCert();
                timeout_cblock();
                break;
            case "bad-hmac":
                System.out.println("bad-hmac: Server will reply with bad HMAC.");
                controller.getBlock();
                controller.postCert();
                controller.pkblock();
                controller.cblock();
                bad_hmac();
                break;

            case "out-order-pk":
                System.out.println("timeout-cb: Server will send an old PKBlock");
                controller.postCert();
                controller.getCert();
                controller.cblock();
                old_pkblock();
                break;
            case "bad-hmac-session":
                System.out.println("bad-hmac-session: Server will reply old sessionId in HMAC.");
                wrongGet();
                controller.postCert();
                controller.pkblock();
                controller.cblock();
                controller.getCert();



                break;

        }

    }

    public static void timeout_pkblock() {
        post("/pkblock", (request, response) -> {
            response.type("application/json");

            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            try {
                String id = blockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

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
            String id = blockFSService.put_h(data);

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

            if(!verifyHMAC(request, SECRET, portS)) {
                System.out.println("HMAC failed ");
                halt(401);
            }


            response.header("Authorization", BlockFSController.buildHMAC(request, "secret", 1));

            List<Certificate> certificateList = new LinkedList<Certificate>();

            for(X509Certificate cert : blockFSService.readPubKeys()) {
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
                    dataBlock = blockFSService.get(id);
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
                String id = blockFSService.put_k(pkBlock.getData(), pkBlock.getSignature(), pkBlock.getPublicKey());

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


    public static void wrongGet() {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            System.out.println("**********");
            System.out.println("**********");
            System.out.println("**********");


            String returnResult;
            String id = request.params(":id");
            System.out.println("GET block:"+id);

            if(!verifyHMAC(request, SECRET, portS)) {
                halt(401);
            }

            response.header("Authorization", buildWrongHMAC(request, SECRET, portS));

            byte[] dataBlock = new byte[0];
            try {
                dataBlock = blockFSService.get(id);
            } catch (FileNotFoundException e) {
                System.out.println("File with id < " + id + " > not found");
                halt(404);
            }

            if(id.startsWith("DATA")) {
                returnResult = Base64.getEncoder().encodeToString(dataBlock);
            }else {
                String json = new String(dataBlock);
                String randomId = request.headers("sessionid");
                String hash = CryptoUtil.generateHash((json + randomId).getBytes());
                response.header("sessionid", hash);


                returnResult = json;
            }

            return returnResult;
        });
    }

    static String oldRandom = "";
    public static String buildWrongHMAC(Request request, String secret, int port) {
        List<String> fields = new LinkedList<>();
        fields.add(request.requestMethod());
        fields.add(request.contentType());

            fields.add(28+"");
        fields.add(request.raw().getPathInfo());
        oldRandom = request.headers("sessionid");
        String message = fields.stream().collect(Collectors.joining("")) + "RESPONSE";
        String secretConcat = "secret" + port;
        System.out.println("buildHMAC:" + message);

        try {
            return CryptoUtil.calculateHMAC(message, secretConcat);
        } catch (SignatureException e) {
            return null;
        }

    }

}
