package com.blockfs.client;

import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

//import java.security.*;

/**
 * Created by joaosampaio on 09-03-2016.
 */
public class KeyStoreClient {


    public static KeyStore loadKeyStone(String keyStoreName, String pass) throws WrongPasswordException {
        KeyStore ks = null;

        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());

        // get user password and file input stream
            char[] password = pass.toCharArray();

            java.io.FileInputStream fis = null;
            try {
                if ( new File(keyStoreName).exists()) {
                    fis = new java.io.FileInputStream( keyStoreName);
                    ks.load(fis, password);
                }else{
                    ks.load(null, password);
                }
            }  catch (NoSuchAlgorithmException | CertificateException e) {
                e.printStackTrace();
            }  catch (IOException e) {
//                e.printStackTrace();
                throw new WrongPasswordException();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return ks;
    }


    public static X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, CertificateEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException {

// build a certificate generator
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal dnName = new X500Principal("cn=example");

// add some options
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(new X509Name("dc=name"));
        certGen.setIssuerDN(dnName); // use the same
// yesterday
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
// in 2 years
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
// finally, sign the certificate with the private key of the same KeyPair
        X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");
        return cert;
    }

    public static KeyPair loadKeyPair(String keyStoreName, String password) throws  WrongPasswordException {
        KeyStore ks = null;
        ks = loadKeyStone(keyStoreName, password);
        Key key = null;
        try {
            key = ks.getKey("private", password.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = ks.getCertificate("certificate");

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void saveKeyStore(String keyStoreName, String password, KeyPair keyPair) throws WrongPasswordException {
        KeyStore ks = null;
        ks = loadKeyStone(keyStoreName, password);
        X509Certificate certificate = null;
        try {
            certificate = generateCertificate(keyPair);
            KeyStore.ProtectionParameter protParam =
                    new KeyStore.PasswordProtection(password.toCharArray());

            // get my private key
            KeyStore.PrivateKeyEntry pkEntry = null;


            Certificate[] certChain = new Certificate[1];
            certChain[0] =  certificate;

            ks.setKeyEntry("private", keyPair.getPrivate(), password.toCharArray(), certChain);

            ks.setCertificateEntry("certificate", certificate);

            // store away the keystore
            java.io.FileOutputStream fos = null;
            try {
                fos = new java.io.FileOutputStream(keyStoreName);
                ks.store(fos, password.toCharArray());
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }




        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }


    }
}
