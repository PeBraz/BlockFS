package com.blockfs.client;


import java.io.*;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CCBlockClient implements ICCBlockClient {


    private IBlockServerRequests blockServer;
    public static int BLOCK_SIZE = 8192; // 8KB

    private X509Certificate cert = null;

    public CCBlockClient (IBlockServerRequests binds) {
        this.blockServer = binds;
    }

    public CCBlockClient () {
        this.blockServer = new BlockServerRequests();
    }



    public void FS_init() throws IBlockServerRequests.IntegrityException {

        this.cert = KeyStoreClient.getCertificateFromCard();
        blockServer.storePubKey(cert);
    }

    public void FS_write(int pos, int size, byte[] contents)
            throws ICCBlockClient.UninitializedFSException, IBlockServerRequests.IntegrityException,
            ServerRespondedErrorException, ClientProblemException {

        if (cert == null) throw new ICCBlockClient.UninitializedFSException();

        if (size > contents.length)
            size = contents.length;

        List<String> hashes = this.getPKB(cert.getPublicKey());

        int block_index = pos / BLOCK_SIZE;

        //previous last block needs to be fully padded
        if (hashes.size() > 0 && block_index >= hashes.size()) {
            byte[] old_block = this.getDB(hashes.get(hashes.size() - 1));
            byte[] new_block = new byte[BLOCK_SIZE];
            System.arraycopy(old_block, 0, new_block, 0, old_block.length);
            hashes.set(hashes.size() - 1, blockServer.put_h(new_block));
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


                byte[] new_block = new byte[new_block_length];

                System.arraycopy(old_block, 0, new_block, 0, old_block.length);


                //Add new content to block from position start
                System.arraycopy(contents, contentsOffset, new_block, startOffset, length);

                if (startOffset + length < old_block.length)
                    System.arraycopy(old_block, startOffset + length, new_block, startOffset + length, old_block.length - (startOffset + length));

                //fingers crossed
                hashes.set(block_index, blockServer.put_h(new_block));
            }
            else {
                byte[] new_block = new byte[startOffset + length];
                System.arraycopy(contents, contentsOffset, new_block, startOffset, length);

                hashes.add(blockServer.put_h(new_block));
            }


            contentsOffset += length;
            startOffset = 0;
            block_index++;
        }

        this.putPKB(hashes);
    }


    public int FS_read(PublicKey pKey, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException {

        if (size > contents.length)
            size = contents.length;

        List<String> hashes = this.getPKB(pKey);
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


    public List<PublicKey> FS_list() {
        return blockServer.readPubKeys();
    }


    private byte[] getDB(String id) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException {
        return blockServer.get("DATA" + id).getData();
    }


    private List<String> getPKB(PublicKey pKey) throws IBlockServerRequests.IntegrityException {
        try {
            byte[] pkBlock = blockServer.get("PK" + new String(pKey.getEncoded())).getData();
            List<String> hashes;
            try (ObjectInputStream ous = new ObjectInputStream(new ByteArrayInputStream(pkBlock))) {
                hashes = (List<String>) ous.readObject();

            } catch (IOException | ClassNotFoundException e) {
                // on first PKB, no file is found so throws exception EOFException (IOException)
                return new ArrayList<>();
            }
            return hashes;


        } catch (ServerRespondedErrorException e) {
            return new ArrayList<>();
        }
    }

    private String putPKB(List<String> hashes) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException, ClientProblemException {
        String pkhash = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(hashes);
            byte[] signature = KeyStoreClient.signWithCard(baos.toByteArray());
            pkhash = blockServer.put_k(baos.toByteArray(), signature, cert.getPublicKey().getEncoded());
            baos.close();
            oos.close();
        } catch ( IOException e) {
            e.printStackTrace();
            throw new ClientProblemException("putPKBWithCard Exception");
        }
        return pkhash;

    }


}
