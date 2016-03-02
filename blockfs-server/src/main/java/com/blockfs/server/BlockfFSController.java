package com.blockfs.server;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Base64;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class BlockfFSController {

    private static Gson GSON = new Gson();
    private static BlockFSService BlockFSService = new BlockFSService();

    public BlockfFSController() {

        get("/block/:id", (request, response) -> {
            response.type("application/json");

            String id = request.params(":id");
            System.out.println(id);

            byte[] dataBlock = BlockFSService.get(id);

            Map<String, String> body = new HashMap<String, String>();
            body.put("data", Base64.getEncoder().encodeToString(dataBlock));

            return GSON.toJson(body);
        });

        post("/pkblock", (request, response) -> {
            response.type("application/json");
            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());
            byte[] signature = Base64.getDecoder().decode(body.get("signature").getAsString());
            byte[] publicKey = Base64.getDecoder().decode(body.get("publicKey").getAsString());

            String id = BlockFSService.put_k(data, signature, publicKey);
            Map<String, String> resBody = new HashMap<String, String>();
            resBody.put("id", id);

            return GSON.toJson(resBody);

        });

        post("/cblock", (request, response) -> {
            response.type("application/json");
            JsonObject body = new JsonParser().parse(request.body()).getAsJsonObject();

            byte[] data = Base64.getDecoder().decode(body.get("data").getAsString());

            String id = BlockFSService.put_h(data);
            Map<String, String> resBody = new HashMap<String, String>();
            resBody.put("id", id);

            return GSON.toJson(resBody);
        });

    }
}
