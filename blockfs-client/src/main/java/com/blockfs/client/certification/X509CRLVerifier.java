package com.blockfs.client.certification;


import com.blockfs.client.exception.X509CertificateVerificationException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.Extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;

public class X509CRLVerifier {

    public void verifyCertificateCRLs(X509Certificate cert) throws X509CertificateVerificationException {

            try {
                List<String> crlDistPoints = getCrlDistributionPoints(cert);

                for(String crlDP : crlDistPoints) {
                    X509CRL crl = downloadCRL(crlDP);

                    if(crl.isRevoked(cert)) {
                        throw new X509CertificateVerificationException("The certificate is revoked by CRL: " + crlDP);
                    }
                }
            } catch (IOException e) {
                throw new X509CertificateVerificationException("Could not verify CRL for certificate: " + cert.getSubjectX500Principal());
            } catch (CRLException e) {
                throw new X509CertificateVerificationException("Could not verify CRL for certificate: " + cert.getSubjectX500Principal());
            } catch (CertificateException e) {
                throw new X509CertificateVerificationException("Could not verify CRL for certificate: " + cert.getSubjectX500Principal());
            }
    }

    public List<String> getCrlDistributionPoints(X509Certificate cert) throws IOException {
        byte[] crldpExt = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());

        if(crldpExt == null) {
            return new ArrayList<String>();
        }

        ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
        DERTaggedObject derObjectCrlDP = (DERTaggedObject) oAsnInStream.readObject();
        ASN1OctetString dosCrlDP = (ASN1OctetString) derObjectCrlDP.getObject();

        byte[] crldpExtOctets = dosCrlDP.getOctets();

        ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
        DERTaggedObject derObject2 = (DERTaggedObject) oAsnInStream2.readObject();

        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObject2);

        List<String> crlUrls = new ArrayList<String>();

        for(DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();

            if(dpn != null) {
                if(dpn.getType() == DistributionPointName.FULL_NAME) {
                    GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();

                    for(int j=0; j < genNames.length; j++) {
                        if(genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                            String url = DERIA5String.getInstance(genNames[j].getName()).getString();
                            crlUrls.add(url);
                        }

                    }
                }

            }

        }

        return crlUrls;
    }

    private X509CRL downloadCRL(String url) throws IOException, CRLException, CertificateException {
        URL crlUrl = new URL(url);
        InputStream crlStream = crlUrl.openStream();

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL) cf.generateCRL(crlStream);
            return crl;
        }finally {
            crlStream.close();
        }

    }
}
