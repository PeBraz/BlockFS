package com.blockfs.server.certification;

import com.blockfs.server.exceptions.X509CertificateVerificationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.*;
import java.util.HashSet;
import java.util.Set;

public class X509CertificateVerifier {

    private X509CRLVerifier crlVerifier;

    public X509CertificateVerifier() {
        this.crlVerifier = new X509CRLVerifier();
    }

    public PKIXCertPathBuilderResult verifyCertificate(X509Certificate certificate, Set<X509Certificate> addCerts) throws X509CertificateVerificationException {
        try {
            if(isSelfSigned(certificate)) {
                throw new X509CertificateVerificationException("Self-signed certificate.");
            }

            Set<X509Certificate> rootCerts = new HashSet<X509Certificate>();
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();

            for(X509Certificate cert : addCerts) {
                if(isSelfSigned(cert)) {
                    rootCerts.add(cert);
                }else {
                 intermediateCerts.add(cert);
                }
            }

            // Build and verify
            PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(certificate, rootCerts, intermediateCerts);

            crlVerifier.verifyCertificateCRLs(certificate);

            return verifiedCertChain;

        } catch (CertificateException e) {
            throw new X509CertificateVerificationException("Error verifying the certificate: " + certificate.getSubjectX500Principal());
        } catch (CertPathBuilderException e) {
            throw new X509CertificateVerificationException("Error building certification path: " + certificate.getSubjectX500Principal());
        } catch (NoSuchAlgorithmException|InvalidKeyException|SignatureException|InvalidAlgorithmParameterException|NoSuchProviderException e ) {
            throw new X509CertificateVerificationException("Error verifying the certificate: " + certificate.getSubjectX500Principal());
        }
    }

    private PKIXCertPathBuilderResult verifyCertificate(X509Certificate certificate, Set<X509Certificate> rootCerts, Set<X509Certificate> intermediateCerts) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, CertPathBuilderException {

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(certificate);

        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for(X509Certificate rootCert : rootCerts) {
            trustAnchors.add(new TrustAnchor(rootCert, null));
        }

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

        pkixParams.setRevocationEnabled(false);

        // List of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(intermediateCerts), "BC");
        pkixParams.addCertStore(intermediateCertStore);

        // Build cert chain
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult)builder.build(pkixParams);

        return result;
    }


    public boolean isSelfSigned(X509Certificate cert) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (InvalidKeyException e) {
            return false;
        } catch (SignatureException e) {
            return false;
        }

    }

}
