package com.blockfs.server;

import com.blockfs.server.models.DataBlock;
import com.blockfs.server.utils.CryptoUtil;

import java.util.HashMap;
import java.util.Map;

public class BlockFS implements IBlockServer
{

    public BlockFS() {
    }

    public byte[] get(String id) {
        return DataBlock.readBlock(id);
    }

    public String put_k(byte[] data, String signature, String publicKey) {

        // TODO: faz hash da data
        // TODO: verifica assinatura com publoc key
        // TODO: compara hashs
        // TODO: excepcao e cenas se nao 401

        return null;
    }

    public String put_h(byte[] data) {
        return null;
    }

}
