package com.blockfs.client;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class BlockClient implements IBlockClient{

    private IBlockServerRequests blockServer;
    public static int BLOCK_SIZE = 8192; // 8KB
    private KeyPair keys;
    private final String KEYS_FILE = "keys.keys";

    public BlockClient (IBlockServerRequests binds) {
        this.blockServer = binds;
    }

    public BlockClient () {
        this.blockServer = new BlockServerRequests();
    }

    public byte[] getPublic() {
        return keys.getPublic().getEncoded();
    }

    /**
     *  Initializes keys and serializes them to a file, future calls will use the old keys
     *
     *  @return hash of current public key
     */

    public String FS_init(String name, String password) throws WrongPasswordException, ClientProblemException {

        if (! new File(name).exists()) {
            try {
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(1024);
                keys = keygen.generateKeyPair();

                KeyStoreClient.saveKeyStore(name, password, keys);

            } catch (NoSuchAlgorithmException e) {
                throw new ClientProblemException("NoSuchAlgorithmException");
            }
        }
        else {
            keys = KeyStoreClient.loadKeyPair(name, password);
        }
        return CryptoUtil.generateHash(keys.getPublic().getEncoded());
    }

    public void FS_write(int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, UninitializedFSException, ServerRespondedErrorException, ClientProblemException {

        if (keys == null) throw new UninitializedFSException();

        if (size > contents.length)
            size = contents.length;

        String id = CryptoUtil.generateHash(keys.getPublic().getEncoded());
        List<String> hashes = this.getPKB(id);

        int block_index = pos / BLOCK_SIZE;

        //previous last block needs to be fully padded
        if (hashes.size() > 0 && block_index == hashes.size()) {
            byte[] old_block = this.getDB(hashes.get(block_index - 1));
            byte[] new_block = new byte[BLOCK_SIZE];
            System.arraycopy(old_block, 0, new_block, 0, old_block.length);
            hashes.set(block_index - 1, blockServer.put_h(new_block));
        }

        //Creates empty blocks for difference between pos given and end of PKB
        //e.g: if there are 5 blocks and block index is 7, need to add block 6 to server
        //  (also need to pad block 5 completely)
        if (block_index > hashes.size())
            for (int i = hashes.size(); i < block_index; i++ )
                hashes.add(blockServer.put_h(new byte[BLOCK_SIZE])); // adds a full padded block

        int contentsOffset = 0;
        int startOffset = pos % BLOCK_SIZE;
        while (contentsOffset < size) {

            int length = Math.min(BLOCK_SIZE - startOffset, size - contentsOffset);
            if (block_index < hashes.size()) {
                byte[] old_block = this.getDB(hashes.get(block_index));
                //new block can be bigger than old block
                int new_block_length = Math.max(old_block.length, startOffset + length);


                byte[] new_block = new byte[new_block_length];    //need to cut excess size after

                System.arraycopy(old_block, 0, new_block, 0, old_block.length);


                //Add new content to block from position start
                System.arraycopy(contents, contentsOffset, new_block, startOffset, length);

                if (size - contentsOffset < old_block.length)
                    System.arraycopy(old_block, startOffset + length, new_block, startOffset + length, old_block.length - (startOffset + length));

                //fingers crossed
                hashes.set(block_index, blockServer.put_h(new_block));
            }
            else {
                byte[] new_block = new byte[startOffset + length];
                System.arraycopy(contents, contentsOffset, new_block, 0, length);

                hashes.add(blockServer.put_h(new_block));
            }


            contentsOffset += length;
            startOffset = 0;
            block_index++;
        }

        this.putPKB(hashes, keys);
    }

    public int FS_read(String hash, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException {

        if (size > contents.length)
            size = contents.length;

        List<String> hashes = this.getPKB(hash);
        int block_index = pos / BLOCK_SIZE;

        if (block_index >= hashes.size()) return 0;

        int startOffset = pos % BLOCK_SIZE;
        int contentsOffset = 0;
        while (contentsOffset < size) {

            if (block_index >= hashes.size()) break;

            byte[] dataBlock = this.getDB(hashes.get(block_index));

            /* Reading length depends:
             *      - if starting to read from middle of block (and not beginning) (dataBlock.length - startOffset)
             *      - if limited by size argument (size - contentsOffset)
             *      - If limited by file size (reading from incomplete block) (dataBlock.length - startOffset)
             */
            int length = Math.min(dataBlock.length - startOffset, size - contentsOffset);
            System.arraycopy(dataBlock, startOffset, contents, contentsOffset, length);

            contentsOffset += length;
            startOffset = 0;
            block_index++;
        }
        return contentsOffset;
    }


    public byte[] getDB(String id) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException {
        byte[] data = blockServer.get("DATA" + id).getData();

        return data;
    }


    public List<String> getPKB(String hash) throws IBlockServerRequests.IntegrityException {
        try {
            byte[] pkBlock = blockServer.get("PK" + hash).getData();
            List<String> hashes;
            try (ObjectInputStream ous = new ObjectInputStream(new ByteArrayInputStream(pkBlock))) {
                hashes = (List<String>) ous.readObject();

            } catch ( IOException | ClassNotFoundException e) {
                // on first PKB, no file is found so throws exception EOFException (IOException)
                return new ArrayList<>();
            }
            return hashes;


        } catch (ServerRespondedErrorException e) {
            return new ArrayList<>();
        }
    }

    public String putPKB(List<String> hashes, KeyPair keys) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException, ClientProblemException {
        String pkhash = "";
        try {
            byte[] signature;
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keys.getPrivate());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(hashes);
            sig.update(baos.toByteArray());
            signature = sig.sign();

            pkhash = blockServer.put_k(baos.toByteArray(), signature, keys.getPublic().getEncoded());

            baos.close();
            oos.close();
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException e) {
            e.printStackTrace();
            throw new ClientProblemException("putPKB Exception");
        }
        return pkhash;
    }

    public KeyPair getKeys() {
        return keys;
    }

}
