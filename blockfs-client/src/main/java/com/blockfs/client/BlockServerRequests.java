package com.blockfs.client;


import com.blockfs.client.certification.X509CertificateVerifier;
import com.blockfs.client.certification.X509Reader;
import com.blockfs.client.connection.ConnectionPool;
import com.blockfs.client.exception.InvalidCertificate;
import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.exception.ValidationException;
import com.blockfs.client.exception.X509CertificateVerificationException;
import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.Block;
import com.blockfs.client.rest.model.PKBlock;
import com.blockfs.client.util.CryptoUtil;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockServerRequests implements IBlockServerRequests{

    private X509Reader x509Reader;
    private X509CertificateVerifier x509CertificateVerifier;
    private KeyStore keyStore;
    public int version = 0;
    private final ConnectionPool pool;

    public BlockServerRequests(){
        this.x509Reader = new X509Reader();
        this.x509CertificateVerifier = new X509CertificateVerifier();
        this.keyStore = x509Reader.loadKeyStore("cc-keystore", "password");
        this.pool = new ConnectionPool(Arrays.asList(Config.ENDPOINTS));
    }

    private void readPublicKeyValidation(String id, Block block) throws ValidationException {
        PKBlock pkblock = (PKBlock)block;
        boolean signed = CryptoUtil.verifySignature(pkblock.getData(), pkblock.getSignature(), pkblock.getPublicKey());

        String hash = "PK" + CryptoUtil.generateHash(pkblock.getPublicKey());

        if (!signed || !hash.equals(id))
            throw new ValidationException();
    }
    private void readDataBlockValidation(String id, Block block) throws ValidationException {
        String hash = "DATA"+CryptoUtil.generateHash(block.getData());
        if(!hash.equals(id))
            throw new ValidationException();
    }


    public Block get(String id) throws ServerRespondedErrorException, IntegrityException {
        System.out.println("Block get");
        if (id.startsWith("PK"))
            return pool.readPK(id, this::readPublicKeyValidation);
        return pool.readCB(id, this::readDataBlockValidation);
    }

    public String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException, ServerRespondedErrorException {

        return  pool.writePK(data, signature, pubKey);

    }
    public String put_h(byte[] data) throws IntegrityException, ServerRespondedErrorException {

        return pool.writeCBlock(data);

    }

    /**
     * Get certificates from server and return to client those that are correctly self-signed
     *
     */
    public List<PublicKey> readPubKeys() throws ServerRespondedErrorException, InvalidCertificate {

        List<X509Certificate> certificates = new ArrayList<>();
        List<PublicKey> pbKeys = new ArrayList<>();
        for(String address : Config.ENDPOINTS){
            certificates.clear(); //TODO mais tarde mudar (mentiroso)
            certificates = RestClient.GET_certificates(address);
            for (X509Certificate cert: certificates) {

                try {
                    if(this.version == CCBlockClient.VERSION_WITH_CARD)
                        this.x509CertificateVerifier.verifyCertificate(cert, keyStore);
                    pbKeys.add(cert.getPublicKey());
                } catch (X509CertificateVerificationException e) {
                    throw new InvalidCertificate("Invalid certificate received.");
                }
            }
        }

        return pbKeys;
    }

    public void storePubKey(X509Certificate certificate) throws IntegrityException, ServerRespondedErrorException {
        pool.storePubKey(certificate);

    }

    public void setVersion(int version) {
        this.version = version;
        if(pool != null)
            pool.version = version;
    }
}
