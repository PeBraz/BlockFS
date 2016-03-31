package com.blockfs.server;

import com.blockfs.server.exceptions.ReplayAttackException;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.rest.model.PKBlock;
import com.blockfs.server.rest.model.PKData;
import com.blockfs.server.utils.CryptoUtil;
import com.blockfs.server.utils.DataBlock;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BlockFSService implements IBlockServer
{

    private static Gson GSON = new Gson();
    private List<X509Certificate> certificates = new LinkedList<X509Certificate>();
    private Map<String, Integer> clientsSequence;

    public BlockFSService(){
        this.clientsSequence = new TreeMap<>();
    }

    public byte[] get(String id) throws FileNotFoundException, WrongDataSignature {

        return DataBlock.readBlock(id);
    }

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature, ReplayAttackException {

        if(!CryptoUtil.verifySignature(data, signature, publicKey)) {
            throw new WrongDataSignature();
        }
        String hash = "PK" + CryptoUtil.generateHash(publicKey);
        PKBlock pkBlock = new PKBlock(data, signature, publicKey);

        PKData hashAndSequence = GSON.fromJson(new String(data), PKData.class);
        if(this.clientsSequence.containsKey(hash)){
            int receivedSequence = hashAndSequence.getSequence();
            int currentSequence = this.clientsSequence.get(hash);

            //check if sequence has been seen before
            if(receivedSequence <= currentSequence){
                throw new ReplayAttackException();
            }
        }
        this.clientsSequence.put(hash, hashAndSequence.getSequence());

        String writeData = GSON.toJson(pkBlock, PKBlock.class);

        DataBlock.writeBlock(writeData.getBytes(), hash);

        return hash;
    }

    public String put_h(byte[] data) {

        String hash = "DATA" + CryptoUtil.generateHash(data);
        DataBlock.writeBlock(data, hash);

        return hash;
    }

    public void storePubKey(X509Certificate certificate) {
        this.certificates.add(certificate);
    }

    public List<X509Certificate> readPubKeys() {
        return certificates;
    }

}
