package com.blockfs.client;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

import com.blockfs.server.utils.CryptoUtil;     //... takes from server

public class BlockClient implements IBlockClient{

    private IBlockServerRequests blockServer;
    public static int BLOCK_SIZE = 1024;
    private KeyPair keys;
    private final String KEYS_FILE = "keys.keys";

    public BlockClient (IBlockServerRequests binds) {
        this.blockServer = binds;
    }

    public BlockClient () {
        this.blockServer = new BlockServerRequests();
    }

    public String getPublic() {
        return keys.getPublic().toString();
    }

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

    public void FS_write(int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException{

        String id = CryptoUtil.generateHash(keys.getPublic().getEncoded());
        List<String> hashes = this.getPKB(id);

        int block_index = pos / BLOCK_SIZE;

        //Creates empty blocks for difference between pos given and end of PKB
        //e.g: if there are 5 blocks and block index is 7, need to add block 6 to server
        if (block_index > hashes.size() + 1)
            for (int i = hashes.size(); i < block_index - 1; i++ )
                hashes.add(blockServer.put_h(new byte[BLOCK_SIZE]));

        int contentsOffset = 0;
        int datablockOffset = pos % BLOCK_SIZE;
        while (size / BLOCK_SIZE > 0) {

            //Gets an old BLOCK or creates a new BLOCK
            byte[] dataBlock = (block_index < hashes.size()) ? this.getDB(hashes.get(block_index))
                    : new byte[BLOCK_SIZE];

            System.arraycopy(contents, contentsOffset, dataBlock, datablockOffset, BLOCK_SIZE);
            String new_hash = blockServer.put_h(dataBlock);

            if (block_index < hashes.size())
                hashes.set(block_index, new_hash);
            else
                hashes.add(new_hash);

            size -= BLOCK_SIZE - datablockOffset;
            contentsOffset += BLOCK_SIZE - datablockOffset;
            datablockOffset = 0;
            block_index++;
        }

        byte[] dataBlock = (block_index < hashes.size()) ? this.getDB(hashes.get(block_index))
                : new byte[BLOCK_SIZE];

        System.arraycopy(contents, contentsOffset, dataBlock, datablockOffset, size % BLOCK_SIZE);
        String new_hash = blockServer.put_h(dataBlock);


        if (block_index < hashes.size())
            hashes.set(block_index, new_hash);
        else
            hashes.add(new_hash);


        this.putPKB(hashes, keys);

    }

    public int FS_read(String hash, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException {
    /*
        if (size > contents.length)
            size = contents.length;

        List<String> hashes = blockServer.getPKB(hash);

        int block_index = pos / BLOCK_SIZE;

        if (block_index >= hashes.size())
    */
        return 0;
    }


    public byte[] getDB(String id) throws IBlockServerRequests.IntegrityException {

        byte[] data = blockServer.get(id);
        if (!id.equals(CryptoUtil.generateHash(data)))
            throw new IBlockServerRequests.IntegrityException();

        return data;
    }


    public List<String> getPKB(String hash) throws IBlockServerRequests.IntegrityException {
        byte[] pkBlock = blockServer.get(hash);

        //Integrity Check ( pkBlock signature is correct )
        //

        List<String> hashes;
        try (ObjectInputStream ous = new ObjectInputStream(new ByteArrayInputStream(pkBlock))) {
            hashes = (List<String>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return hashes;
    }

    public String putPKB(List<String> hashes, KeyPair keys) throws IBlockServerRequests.IntegrityException {
        String pkhash = "";
        try {
            byte[] signature;
            Signature sig = null;
            sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keys.getPrivate());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(hashes);
            sig.update(baos.toByteArray());
            signature = sig.sign();

            pkhash = blockServer.put_k(baos.toByteArray(), new String(signature), keys.getPublic().toString());

            baos.close();
            oos.close();
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException e) {
            e.printStackTrace();
        }
        return pkhash;
    }


}
