package com.blockfs.server.certification;

import com.blockfs.server.exceptions.ReadCertificateFileException;
import org.bouncycastle.util.io.pem.PemObjectParser;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class X509Reader {

    public static KeyStore loadKeyStore(String keyStoreName, String pass) {
        KeyStore ks = null;

        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());

            java.io.FileInputStream fis = null;

            try {
                    fis = new java.io.FileInputStream(keyStoreName);
                    ks.load(fis, pass.toCharArray());
            }  catch (NoSuchAlgorithmException | CertificateException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace();
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
}
