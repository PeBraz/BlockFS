package com.blockfs.server;

import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.utils.DataBlock;
import com.blockfs.server.utils.CryptoUtil;
import com.google.gson.Gson;


import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class BlockFSService implements IBlockServer
{

    private static Gson GSON = new Gson();

    public byte[] get(String id) {
        return DataBlock.readBlock(id);
    }

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature {

        if(!CryptoUtil.verifySignature(data, signature, publicKey)) {
            throw new WrongDataSignature();
        }

        String hash = CryptoUtil.generateHash(publicKey);

        Map<String, String> blockData = new HashMap<String, String>();
        blockData.put("data", Base64.getEncoder().encodeToString(data));
        blockData.put("signature", Base64.getEncoder().encodeToString(signature));
        blockData.put("publicKey", Base64.getEncoder().encodeToString(publicKey));

        String writeData = GSON.toJson(blockData);

        DataBlock.writeBlock(writeData.getBytes(), hash);

        return hash;
    }

    public String put_h(byte[] data) {

        String hash = CryptoUtil.generateHash(data);
        DataBlock.writeBlock(data, hash);

        return hash;
    }

}
