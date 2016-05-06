package com.blockfs.client;


import com.blockfs.client.certification.X509CertificateVerifier;
import com.blockfs.client.certification.X509Reader;
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
import java.util.List;

public class BlockServerRequests implements IBlockServerRequests{

    private X509Reader x509Reader;
    private X509CertificateVerifier x509CertificateVerifier;
    private KeyStore keyStore;
    public int version = 0;

    public BlockServerRequests(){
        this.x509Reader = new X509Reader();
        this.x509CertificateVerifier = new X509CertificateVerifier();
        this.keyStore = x509Reader.loadKeyStore("cc-keystore", "password");
    }

    private void readPublicKeyValidation(Block block, String id) throws ValidationException {
        PKBlock pkblock = (PKBlock)block;
        boolean signed = CryptoUtil.verifySignature(pkblock.getData(), pkblock.getSignature(), pkblock.getPublicKey());

        String hash = "PK" + CryptoUtil.generateHash(pkblock.getPublicKey());

        if (!signed || !hash.equals(id))
            throw new ValidationException();
    }


    public Block get(String id) throws ServerRespondedErrorException, IntegrityException {
        Block result = null;
        for(String address : Config.ENDPOINTS) {
            result = RestClient.GET(id, address);
            if(result.getType() == Block.PUBLIC){
                PKBlock pub = (PKBlock)result;
                String hash = "PK"+ CryptoUtil.generateHash(pub.getPublicKey());
                if(!hash.equals(id))
                    throw new IntegrityException("GET: Invalid public block received");

                if(!CryptoUtil.verifySignature(pub.getData(), pub.getSignature(), pub.getPublicKey())){
                    throw new IntegrityException("GET: Public key block signature integrity failed");
                }
            }else if(result.getType() == Block.DATA){
                String hash = "DATA"+CryptoUtil.generateHash(result.getData());
                if(!hash.equals(id))
                    throw new IntegrityException("GET: Invalid data block received");
            }
        }


        return result;
    }

    public String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException, ServerRespondedErrorException {

        String pkHash = "";
        for(String address : Config.ENDPOINTS){
            pkHash = RestClient.POST_pkblock(data, signature, pubKey, address);

            if (!CryptoUtil.generateHash(pubKey).equals(pkHash))
                throw new IntegrityException("PUT_K: invalid public key hash received");
        }


        return pkHash;
    }
    public String put_h(byte[] data) throws IntegrityException, ServerRespondedErrorException {

        String dataHash = "";
        for(String address : Config.ENDPOINTS){
            dataHash = RestClient.POST_cblock(data, address);
            if (!CryptoUtil.generateHash(data).equals(dataHash))
                throw new IntegrityException("PUT_H: invalid data hash received");

        }

        return dataHash;
    }

    /**
     * Get certificates from server and return to client those that are correctly self-signed
     *
     */
    public List<PublicKey> readPubKeys() throws ServerRespondedErrorException, InvalidCertificate {

        List<X509Certificate> certificates = new ArrayList<>();
        List<PublicKey> pbKeys = new ArrayList<>();
        for(String address : Config.ENDPOINTS){
            certificates.clear(); //TODO mais tarde mudar
            certificates = RestClient.GET_certificates(address);
            for (X509Certificate cert: certificates) {

                try {
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
        for(String address : Config.ENDPOINTS){
            RestClient.POST_certificate(certificate, this.version, address);
        }

    }

    public void setVersion(int version) {
        this.version = version;
    }
}
