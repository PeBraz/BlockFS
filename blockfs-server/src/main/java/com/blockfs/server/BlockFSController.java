package com.blockfs.server;

import com.blockfs.server.exceptions.ReplayAttackException;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.BlockId;
import com.blockfs.server.rest.model.Certificate;
import com.blockfs.server.rest.model.PKBlock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.*;

public class BlockFSController {

    private static Gson GSON = new Gson();
    private static BlockFSService BlockFSService = new BlockFSService();

    public BlockFSController() {

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
                returnResult = new String(dataBlock);
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
                return GSON.toJson(blockId);
            }catch (WrongDataSignature e) {
                halt(400);
                return "";
            }catch (ReplayAttackException e){
                halt(401);
                return "";
            }

        });

        post("/cblock", (request, response) -> {
            response.type("application/json");

            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());
            String id = BlockFSService.put_h(data);

            BlockId blockId = new BlockId(id);

            return GSON.toJson(blockId);

        });

        post("/cert", (request, response) -> {
            response.type("application/json");
            System.out.println("cert POst:");
            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();
            byte[] certificate = Base64.getDecoder().decode(body.get("certificate").getAsString());

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(certificate);

            X509Certificate cert = (X509Certificate)certificateFactory.generateCertificate(in);



            BlockFSService.storePubKey(cert);

//            X509Certificate cert = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), X509Certificate.class);
//            BlockFSService.storePubKey(cert);

            return "Certificate saved.";

        });

        get("/cert", (request, response) -> {
            response.type("application/json");
            System.out.println("cert GET:");
            List<Certificate> certificateList = new LinkedList<Certificate>();

            for(X509Certificate cert : BlockFSService.readPubKeys()) {
                certificateList.add(new Certificate(cert.getSubjectDN().getName(), cert.getEncoded()));
            }
            return GSON.toJson(certificateList);
        });
    }
}
