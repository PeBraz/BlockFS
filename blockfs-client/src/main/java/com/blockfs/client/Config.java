package com.blockfs.client;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final String BLOCK_DIR = "../data/";
    public final static Map<String, String> ENDPOINTS;

    static {
        ENDPOINTS = new HashMap<String, String>();
        ENDPOINTS.put("http://0.0.0.0:5050/", "secret5050");
        ENDPOINTS.put("http://0.0.0.0:5051/", "secret5051");
        ENDPOINTS.put("http://0.0.0.0:5052/", "secret5052");
        ENDPOINTS.put("http://0.0.0.0:5053/", "secret5053");
        ENDPOINTS.put("http://0.0.0.0:5054/", "secret5054");
    };

    public final static boolean enableCardTests = false;

}
