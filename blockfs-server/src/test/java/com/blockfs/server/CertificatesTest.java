package com.blockfs.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CertificatesTest extends TestCase
{
    public CertificatesTest(String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( CertificatesTest.class );
    }

    public void testWriteBlock() {

//        BlockFSService service = new BlockFSService();
//        Set<X509Certificate> root = service.getRootCertificates();
//        X509CertificateVerifier verifier = new X509CertificateVerifier();
//
//        for (X509Certificate c: root) {
//
//            try {
//                System.out.println("**"+c.getSigAlgName());
//                System.out.println("---"+verifier.isSelfSigned(c));
//            } catch (NoSuchProviderException e) {
//                e.printStackTrace();
//            } catch (CertificateException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (SignatureException e) {
//                e.printStackTrace();
//            }
//
//        }


    }


}
