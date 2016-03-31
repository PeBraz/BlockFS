package com.blockfs.server.certification;

import com.blockfs.server.exceptions.ReadCertificateFileException;
import org.bouncycastle.util.io.pem.PemObjectParser;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class X509Reader {
    public X509Certificate readCertificate(String filename) throws IOException, CertificateException {

        BufferedInputStream bis = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(filename));

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        X509Certificate cert = null;

        while(bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
        }

        return cert;
    }

    public Set<X509Certificate> readCertificates(String[] filenames) throws ReadCertificateFileException {

        try {
            Set<X509Certificate> certificates = new HashSet<X509Certificate>();

            for(String filename : filenames) {
                certificates.add(readCertificate(filename));
            }

            return certificates;

        } catch (IOException e) {
            throw new ReadCertificateFileException("Error reading file.");
        } catch (CertificateException e) {
            throw new ReadCertificateFileException("Error generating certificate from file.");
        }
    }
}
