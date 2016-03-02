package com.blockfs.server;

import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.utils.DataBlock;
import com.blockfs.server.utils.CryptoUtil;

public class BlockFSService implements IBlockServer
{

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

        String hash = CryptoUtil.generateHash(data);
        DataBlock.writeBlock(data, hash);

        return hash;
    }

}
