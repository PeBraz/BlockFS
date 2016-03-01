package com.blockfs.client;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BlockServerRequests implements IBlockServerRequests{

    public byte[] get(String id) {
        //TODO
        return new byte[0];
    }

    public String put_k(byte[] data, String signature, String pubKey) {
        //TODO
        return "";
    }
    public String put_h(byte[] data) {
        //TODO
        return "";
    }
    public List<String> getPKB(String hash) {
        byte[] pkBlock = this.get(hash);

        List<String> hashes;
        try (ObjectInputStream ous = new ObjectInputStream(new ByteArrayInputStream(pkBlock))) {
            hashes = (List<String>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return hashes;
    }

    public void putPKB(List<String> hashes, String signature, String pubKey) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream ous = new ObjectOutputStream(baos)) {
            ous.writeObject(hashes);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        put_k(baos.toByteArray(), signature, pubKey);
    }
}
