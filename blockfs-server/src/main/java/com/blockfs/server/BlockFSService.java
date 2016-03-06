package com.blockfs.server;

import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.PKBlock;
import com.blockfs.server.utils.CryptoUtil;
import com.blockfs.server.utils.DataBlock;
import com.google.gson.Gson;

import java.io.FileNotFoundException;

public class BlockFSService implements IBlockServer
{

    private static Gson GSON = new Gson();

    public byte[] get(String id) throws FileNotFoundException {
        return DataBlock.readBlock(id);
    }

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature {

        if(!CryptoUtil.verifySignature(data, signature, publicKey)) {
            throw new WrongDataSignature();
        }

        String hash = "PK"+CryptoUtil.generateHash(publicKey);

        PKBlock pkBlock = new PKBlock(data, signature, publicKey);
        String writeData = GSON.toJson(pkBlock, PKBlock.class);

        DataBlock.writeBlock(writeData.getBytes(), hash);

        return hash;
    }

    public String put_h(byte[] data) {

        String hash = "DATA"+CryptoUtil.generateHash(data);
        DataBlock.writeBlock(data, hash);

        return hash;
    }

}
