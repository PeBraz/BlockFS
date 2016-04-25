package com.blockfs.server;

import com.blockfs.server.certification.X509CertificateVerifier;
import com.blockfs.server.certification.X509Reader;
import com.blockfs.server.exceptions.InvalidCertificate;
import com.blockfs.server.exceptions.ReplayAttackException;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.exceptions.X509CertificateVerificationException;
import com.blockfs.server.rest.model.PKBlock;
import com.blockfs.server.rest.model.PKData;
import com.blockfs.server.utils.CryptoUtil;
import com.blockfs.server.utils.DataBlock;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;

public class BlockFSService implements IBlockServer
{

    private static Gson GSON = new Gson();
    private Set<X509Certificate> certificates = new HashSet<X509Certificate>();
    private X509Reader x509Reader;
    private X509CertificateVerifier x509CertificateVerifier;
    private KeyStore keyStore;
    private Map<String, Integer> clientsSequence;
    private int port;

    public BlockFSService() {
        this.x509Reader = new X509Reader();
        this.x509CertificateVerifier = new X509CertificateVerifier();
        this.keyStore = x509Reader.loadKeyStore("cc-keystore", "password");
        this.clientsSequence = new TreeMap<>();
    }

    public byte[] get(String id) throws FileNotFoundException, WrongDataSignature {

        return DataBlock.readBlock(id, this.port);
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
                System.out.println("ReplayAttack  receivedSequence:"+receivedSequence + "  currentSequence:"+currentSequence);
                throw new ReplayAttackException();
            }
        }
        this.clientsSequence.put(hash, hashAndSequence.getSequence());

        String writeData = GSON.toJson(pkBlock, PKBlock.class);

        DataBlock.writeBlock(writeData.getBytes(), hash, this.port);

        return hash;
    }

    public String put_h(byte[] data) {

        String hash = "DATA" + CryptoUtil.generateHash(data);
        DataBlock.writeBlock(data, hash, this.port);

        return hash;
    }

    public void storePubKey(X509Certificate certificate, boolean verify) throws InvalidCertificate {
        try {
            if(verify)
                this.x509CertificateVerifier.verifyCertificate(certificate, keyStore);
            this.certificates.add(certificate);
        } catch (X509CertificateVerificationException e) {
            e.printStackTrace();
            throw new InvalidCertificate("Invalid certificate received.");
        }
    }

    public List<X509Certificate> readPubKeys() {
        List<X509Certificate> certs = new LinkedList<X509Certificate>();
        certs.addAll(this.certificates);
        return certs;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
