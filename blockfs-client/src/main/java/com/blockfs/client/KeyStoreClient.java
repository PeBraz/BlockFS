package com.blockfs.client;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.Date;
import java.util.Random;

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


    public static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        X509Certificate cert = (X509Certificate)f.generateCertificate(in);
        return cert;
    }

    // Returns the n-th certificate, starting from 0
    public static  byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            System.out.println("Number of certs found: " + certs.length);
            int i = 0;
//            for (PTEID_Certif cert : certs) {
//                System.out.println("-------------------------------\nCertificate #"+(i++));
//                System.out.println(cert.certifLabel);
//            }

            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

            //pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a eID Lib
        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }

    public static X509Certificate getCertificateFromCard() {
        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String libName = "libbeidpkcs11.so";
        System.loadLibrary("pteidlibj");

        try {
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
            X509Certificate cert =  getCertFromByteArray(getCertificateInBytes(0));
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
            return cert;
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        } catch (PteidException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static PublicKey getPubKeyFromCard(){
        PublicKey publicKey = null;
        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String libName = "libbeidpkcs11.so";
        System.loadLibrary("pteidlibj");


        try {
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
            X509Certificate cert = getCertFromByteArray(getCertificateInBytes(0));
            publicKey = cert.getPublicKey();
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
        } catch (CertificateException e) {

            e.printStackTrace();
        } catch (PteidException e) {
            e.printStackTrace();
        }

        return publicKey;
    }

    public static byte[] signWithCard(byte[] data){

        byte[] signature = null;
        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String libName = "libbeidpkcs11.so";
        if (-1 != osName.indexOf("Windows"))
            libName = "pteidpkcs11.dll";
        else if (-1 != osName.indexOf("Mac"))
            libName = "pteidpkcs11.dylib";
        Class pkcs11Class = null;
        try {
            pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
            if (javaVersion.startsWith("1.5."))
            {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
                pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
            }
            else
            {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
                pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
            }


            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            // Token login
            System.out.println("            //Token login");
            pkcs11.C_Login(p11_session, 1, null);
            CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);


            // Get available keys
            System.out.println("            //Get available keys");
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
            attributes[0] = new CK_ATTRIBUTE();
            attributes[0].type = PKCS11Constants.CKA_CLASS;
            attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

            pkcs11.C_FindObjectsInit(p11_session, attributes);
            long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

            // points to auth_key
            System.out.println("            //points to auth_key. No. of keys:"+keyHandles.length);

            long signatureKey = keyHandles[0];		//test with other keys to see what you get
            pkcs11.C_FindObjectsFinal(p11_session);

            // initialize the signature method
            System.out.println("            //initialize the signature method");
            CK_MECHANISM mechanism = new CK_MECHANISM();
            mechanism.mechanism = PKCS11Constants.CKM_SHA256_RSA_PKCS;
            mechanism.pParameter = null;
            pkcs11.C_SignInit(p11_session, mechanism, signatureKey);

            // sign
            System.out.println("            //sign");
            signature = pkcs11.C_Sign(p11_session, data);
            System.out.println("            //signature:"+encoder.encode(signature));



            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (PKCS11Exception e) {
            e.printStackTrace();
        } catch (PteidException e) {
            e.printStackTrace();
        }



        return signature;

    }



}
