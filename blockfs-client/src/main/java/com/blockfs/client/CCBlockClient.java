package com.blockfs.client;


import com.blockfs.client.exception.*;
import com.blockfs.client.rest.model.PKData;
import com.google.gson.Gson;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CCBlockClient implements ICCBlockClient {

    private ReplayAttackSolution clientSequence;
    private IBlockServerRequests blockServer;
    public static int BLOCK_SIZE = 8192; // 8KB

    private X509Certificate cert = null;

    public CCBlockClient (IBlockServerRequests binds) {
        this.blockServer = binds;
        this.clientSequence = new ReplayAttackSolution();
    }

    public CCBlockClient () {
        this.blockServer = new BlockServerRequests();
        this.clientSequence = new ReplayAttackSolution();
    }



    public void FS_init()
            throws NoCardDetectedException, IBlockServerRequests.IntegrityException, ServerRespondedErrorException {

        this.cert = CardReaderClient.getCertificateFromCard();
        blockServer.storePubKey(cert);
    }

    public void FS_write(int pos, int size, byte[] contents)
            throws ICCBlockClient.UninitializedFSException , IBlockServerRequests.IntegrityException ,
           ServerRespondedErrorException, ClientProblemException, WrongCardPINException {

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


    public int FS_read(PublicKey pKey, int pos, int size, byte[] contents)
            throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException {

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

    public List<PublicKey> FS_list() throws ServerRespondedErrorException, InvalidCertificate {
        return blockServer.readPubKeys();
    }


    private byte[] getDB(String id) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException {
        return blockServer.get("DATA" + id).getData();
    }


    private List<String> getPKB(PublicKey pKey) throws IBlockServerRequests.IntegrityException {
        try {
            byte[] pkBlock = blockServer.get("PK" + CryptoUtil.generateHash(pKey.getEncoded()) ).getData();
            List<String> hashes;

            Gson gson = new Gson();
            PKData hashAndSequence = gson.fromJson(new String(pkBlock), PKData.class);

            if(hashAndSequence == null)
                hashes = new ArrayList<>();
            else
                hashes = hashAndSequence.getHashes();

            return hashes;

        } catch (ServerRespondedErrorException e) {
            return new ArrayList<>();
        }
    }

    private String putPKB(List<String> hashes)
            throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException,
            ClientProblemException, WrongCardPINException {

        String pkhash = "";

        int sequence = clientSequence.getValidSequence(CryptoUtil.generateHash(cert.getPublicKey().getEncoded()));
        PKData hashAndSequence = new PKData(sequence, hashes);
        Gson gson = new Gson();
        byte[] data = gson.toJson(hashAndSequence).getBytes();
        byte[] signature = CardReaderClient.signWithCard(data);
        pkhash = blockServer.put_k(data, signature, cert.getPublicKey().getEncoded());

        return pkhash;

    }




}
