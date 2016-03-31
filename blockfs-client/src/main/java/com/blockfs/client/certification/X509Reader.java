package com.blockfs.client.certification;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class X509Reader {

    public static KeyStore loadKeyStore(String keyStoreName, String pass) {
        KeyStore ks = null;

        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());

            FileInputStream fis = null;

            try {
                    fis = new FileInputStream(keyStoreName);
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
