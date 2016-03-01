package com.blockfs.client;

import java.io.*;
import java.security.*;
import java.util.List;

import com.blockfs.server.utils.CryptoUtil;     //...

public class BlockClient implements IBlockClient{

    private final IBlockServerRequests blockServer = new BlockServerRequests();
    private final int BLOCK_SIZE = 1024;
    private KeyPair keys;
    private final String KEYS_FILE = "keys.keys";

    /**
     *  Initializes keys and serializes them to a file, future calls will use the old keys
     */
    public void PS_init() {

        if (new File("keys").exists()) {

            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(KEYS_FILE));
                keys = (KeyPair) ois.readObject();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            keys = keygen.generateKeyPair();
            ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(KEYS_FILE));
            ous.writeObject(keys);
            ous.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }

    public void FS_write(int pos, int size, byte[] contents) {

        String id = CryptoUtil.generateHash(keys.getPublic().getEncoded());
        List<String> hashes = blockServer.getPKB(id);

        int block_index = pos / BLOCK_SIZE;


        int contentsOffset = 0;
        while (size / BLOCK_SIZE > 0) {

            byte[] dataBlock = blockServer.get(hashes.get(block_index));
            System.arraycopy(contents, contentsOffset, dataBlock, 0, BLOCK_SIZE);
            blockServer.put_h(dataBlock);

            size -= BLOCK_SIZE;
            contentsOffset += BLOCK_SIZE;
        }
        // TODO: TAMBEM E NECESSARIO AUMENTAR O TAMANHO DO BLOCO E CRIAR MAIS BLOCOS COM PADDING, CASO NECESSARIO
        byte[] dataBlock = blockServer.get(hashes.get(block_index));
        System.arraycopy(contents, contentsOffset, dataBlock, 0, size % BLOCK_SIZE);


        //TODO: sign this ->  blockServer.putPKB(hashes,  ...   , keys.getPublic());
    }

    public int FS_read(String hash, int pos, int size, byte[] contents) {
        return 0;
    }
}
