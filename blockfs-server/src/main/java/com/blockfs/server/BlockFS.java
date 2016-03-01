package com.blockfs.server;

import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.models.DataBlock;
import com.blockfs.server.utils.CryptoUtil;

public class BlockFS implements IBlockServer
{

    public BlockFS() {
    }

    public byte[] get(String id) {
        return DataBlock.readBlock(id);
    }

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature {

        if(!CryptoUtil.verifySignature(data, signature, publicKey)) {
            throw new WrongDataSignature();
        }

        String hash = CryptoUtil.generateHash(publicKey);
        DataBlock.writeBlock(data, hash);

        return hash;
    }

    public String put_h(byte[] data) {

        String blockId = CryptoUtil.generateHash(data);

        return blockId;
    }

}
