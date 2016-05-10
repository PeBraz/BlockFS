package com.blockfs.client.util;

import com.blockfs.client.exception.WrongPasswordException;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

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
import java.util.Random;

public class KeyStoreClient {

    private static final String BLOCK_DIR = "../data/";

    public static KeyStore loadKeyStone(String keyStoreName, String pass) throws WrongPasswordException {
        KeyStore ks = null;
        keyStoreName = BLOCK_DIR + keyStoreName;
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


    public static X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, InvalidKeyException, SignatureException, CertIOException, OperatorCreationException {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Date dateOfIssuing = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date dateOfExpiry = new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000);

        // signers name
        X500Name issuerName = new X500Name("CN=G15");

        // subjects name - the same as we are self signed.
        X500Name subjectName = issuerName;

        // serial
        BigInteger serial = BigInteger.valueOf(new Random().nextInt());

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(issuerName, serial, dateOfIssuing, dateOfExpiry, subjectName, SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

        v3CertGen.addExtension(
                X509Extension.subjectKeyIdentifier,
                false,
                extUtils.createSubjectKeyIdentifier(keyPair.getPublic()));

        v3CertGen.addExtension(
                X509Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(keyPair.getPublic()));

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(v3CertGen.build(new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(keyPair.getPrivate())));
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
        keyStoreName = BLOCK_DIR + keyStoreName;
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
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (CertIOException e) {
            e.printStackTrace();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }
    }


}
