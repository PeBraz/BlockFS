package com.blockfs.client;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class ReplayAttackSolution {

    private final static String NAME = "listSequence";
    private Map<String, Integer> clientsSequence;
    private static Gson GSON = new Gson();

    public ReplayAttackSolution() {
        this.clientsSequence = new TreeMap<>();

        if ( new File(NAME).exists()) {
            try {
                String json = readFile(NAME);
                clientsSequence = GSON.fromJson(json, new TypeToken<TreeMap<String, Integer>>(){}.getType());

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(clientsSequence == null)
                clientsSequence = new TreeMap<>();

        }else{
            //we create new one
            clientsSequence = new TreeMap<>();
            save();
        }
    }

    private String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }

    private void save(){
        try(FileWriter file = new FileWriter(NAME)) {
            file.write(GSON.toJson(clientsSequence));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getValidSequence(String hash){
        int value = 0;
        if(this.clientsSequence.containsKey(hash)) {
            value = this.clientsSequence.get(hash);
            value++;
            this.clientsSequence.put(hash, value);
        }else{
            this.clientsSequence.put(hash, 0);
        }

        save();
        return value;
    }
}
