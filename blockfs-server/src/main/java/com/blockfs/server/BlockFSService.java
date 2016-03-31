package com.blockfs.server;

import com.blockfs.server.certification.X509CertificateVerifier;
import com.blockfs.server.certification.X509Reader;
import com.blockfs.server.exceptions.InvalidCertificate;
import com.blockfs.server.exceptions.ReadCertificateFileException;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.exceptions.X509CertificateVerificationException;
import com.blockfs.server.rest.model.PKBlock;
import com.blockfs.server.utils.CryptoUtil;
import com.blockfs.server.utils.DataBlock;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BlockFSService implements IBlockServer
{

    private static Gson GSON = new Gson();
    private List<X509Certificate> certificates = new LinkedList<X509Certificate>();
    private X509Reader x509Reader;
    private X509CertificateVerifier x509CertificateVerifier;
    private KeyStore keyStore;

    public BlockFSService() {
        this.x509Reader = new X509Reader();
        this.x509CertificateVerifier = new X509CertificateVerifier();
        this.keyStore = x509Reader.loadKeyStore("cc-keystore", "password");
    }

    public byte[] get(String id) throws FileNotFoundException, WrongDataSignature {

        return DataBlock.readBlock(id);
    }

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature {

        if(!CryptoUtil.verifySignature(data, signature, publicKey)) {
            throw new WrongDataSignature();
        }

        String hash = "PK" + CryptoUtil.generateHash(publicKey);

        PKBlock pkBlock = new PKBlock(data, signature, publicKey);
        String writeData = GSON.toJson(pkBlock, PKBlock.class);

        DataBlock.writeBlock(writeData.getBytes(), hash);

        return hash;
    }

    public String put_h(byte[] data) {

        String hash = "DATA" + CryptoUtil.generateHash(data);
        DataBlock.writeBlock(data, hash);

        return hash;
    }

    public void storePubKey(X509Certificate certificate) throws InvalidCertificate {
        try {
            this.x509CertificateVerifier.verifyCertificate(certificate, keyStore);
            this.certificates.add(certificate);
        } catch (X509CertificateVerificationException e) {
            throw new InvalidCertificate("Invalid certificate received.");
        }
    }

    public List<X509Certificate> readPubKeys() {
        return certificates;
    }

}
