package com.blockfs.server;

import com.blockfs.server.utils.CryptoUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.security.*;

public class CryptoUtilTest extends TestCase
{
    public CryptoUtilTest(String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( CryptoUtilTest.class );
    }

    public void testVerifySignature()
    {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keyPair = keygen.generateKeyPair();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keyPair.getPrivate());

            String data = "HELLO HELLO";
            sig.update(data.getBytes());
            byte[] signature = sig.sign();

            boolean result = CryptoUtil.verifySignature(data.getBytes(), signature, keyPair.getPublic().getEncoded());

            assertTrue(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void testVerifyFakeSignature()
    {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keyPair = keygen.generateKeyPair();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keyPair.getPrivate());

            String data = "HELLO HELLO";
            sig.update(data.getBytes());
            byte[] signature = sig.sign();

            signature[0] |= 0xFF;
            boolean result = CryptoUtil.verifySignature(data.getBytes(), signature, keyPair.getPublic().getEncoded());

            assertFalse(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void testHashGeneration() {

        KeyPairGenerator keygen = null;
        try {
            keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keyPair = keygen.generateKeyPair();
            String result = CryptoUtil.generateHash(keyPair.getPublic().getEncoded());

            assertEquals(result.length(), 56);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
