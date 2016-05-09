package com.blockfs.client;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public final static Map<String, String> ENDPOINTS;

    static {
        ENDPOINTS = new HashMap<String, String>();
        ENDPOINTS.put("http://0.0.0.0:5050/", "secret1");
        ENDPOINTS.put("http://0.0.0.0:5051/", "secret2");
        ENDPOINTS.put("http://0.0.0.0:5052/", "secret3");
        ENDPOINTS.put("http://0.0.0.0:5053/", "secret4");
        ENDPOINTS.put("http://0.0.0.0:5054/", "secret5");
    };

    public final static boolean enableCardTests = false;

}
