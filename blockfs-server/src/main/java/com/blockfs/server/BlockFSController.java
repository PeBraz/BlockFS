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
import spark.Request;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class BlockFSController {

    private static Gson GSON = new Gson();
    private static BlockFSService BlockFSService = new BlockFSService();
    private static final String SECRET = "secret";
    public static int portSpark;

    public BlockFSController(int portSpark) {
        port(portSpark);

        BlockFSController.portSpark = portSpark;

        BlockFSService.setPort(portSpark);

    }

    public void init() {

        getBlock();

        pkblock();

        cblock();

        postCert();

        getCert();

    }


    public static void pkblock() {
        post("/pkblock", (request, response) -> {
            response.type("application/json");

            if(!verifyHMAC(request, SECRET, portSpark)) {
                halt(401);
            }

            PKBlock pkBlock = GSON.fromJson(new JsonParser().parse(request.body()).getAsJsonObject(), PKBlock.class);
            response.header("Authorization", buildHMAC(request, SECRET, portSpark));

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

    public static void getCert() {
        get("/cert", (request, response) -> {
            response.type("application/json");
            System.out.println("Port: " + portSpark);

            if(!verifyHMAC(request, SECRET, portSpark)) {
                halt(401);
            }

            response.header("Authorization", buildHMAC(request, SECRET, portSpark));

            System.out.println("cert GET:");
            List<Certificate> certificateList = new LinkedList<Certificate>();

            for(X509Certificate cert : BlockFSService.readPubKeys()) {
                certificateList.add(new Certificate(cert.getSubjectDN().getName(), cert.getEncoded()));
            }
            return GSON.toJson(certificateList);
        });
    }

    public static void postCert() {
        post("/cert", (request, response) -> {
            response.type("application/json");

            System.out.println("HMAC SECRET: "+SECRET);
            System.out.println("HMAC portSpark: "+portSpark);
            if(!verifyHMAC(request, SECRET, portSpark)) {
                System.out.println("HMAC failed ");
                halt(401);
            }



            response.header("Authorization", buildHMAC(request, SECRET, portSpark));
            response.header("Content-Type", "application/json");
            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();
            byte[] certificate = Base64.getDecoder().decode(body.get("certificate").getAsString());

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(certificate);

            X509Certificate cert = (X509Certificate)certificateFactory.generateCertificate(in);
            System.out.println("cert POst:" + CryptoUtil.generateHash(cert.getPublicKey().getEncoded()));
            try {
                BlockFSService.storePubKey(cert, isVersionWithCard(request));
            }catch(InvalidCertificate e) {
                e.printStackTrace();
                halt(400);
                return "Invalid certificate.";
            }

            return "Certificate saved.";

        });
    }

    public static void cblock() {
        post("/cblock", (request, response) -> {
            response.type("application/json");

            if(!verifyHMAC(request, SECRET, portSpark)) {
                halt(401);
            }

            System.out.println("cert GET:");

            response.header("Authorization", buildHMAC(request, SECRET, portSpark));
            List<Certificate> certificateList = new LinkedList<Certificate>();

            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());
            String id = BlockFSService.put_h(data);

            BlockId blockId = new BlockId(id);

            return GSON.toJson(blockId);

        });
    }

    public static void getBlock() {
        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String returnResult;
            String id = request.params(":id");
            System.out.println("GET block:"+id);

            if(!verifyHMAC(request, SECRET, portSpark)) {
                halt(401);
            }

            response.header("Authorization", buildHMAC(request, SECRET, portSpark));

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
                String json = new String(dataBlock);
                String randomId = request.headers("sessionid");
                String hash = CryptoUtil.generateHash((json + randomId).getBytes());
                response.header("sessionid", hash);


                returnResult = json;
            }

            return returnResult;
        });
    }

    //returns true if version = 2 or no version in header
    public static boolean isVersionWithCard(Request request){
        String version = request.headers("version");
        return (version == null || version.equals("V2"));
    }

    public static boolean verifyHMAC(Request request, String secret, int port) {
        List<String> fields = new LinkedList<>();

        fields.add(request.requestMethod());
        fields.add(request.contentType());
        fields.add(request.headers("sessionid"));
        fields.add(request.raw().getPathInfo());

        String message = fields.stream().collect(Collectors.joining(""));
        String secretConcat = "secret" + port;
        System.out.println("verifyHMAC:" + message);
        System.out.println("Authorization:" + request.headers("Authorization"));
        try {
            return CryptoUtil.verifyHMAC(message, secretConcat, request.headers("Authorization"));
        } catch (SignatureException e) {
            return false;
        }

    }

    public static String buildHMAC(Request request, String secret, int port) {
        List<String> fields = new LinkedList<>();
        fields.add(request.requestMethod());
        fields.add(request.contentType());
        fields.add(request.headers("sessionid"));
        fields.add(request.raw().getPathInfo());

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
