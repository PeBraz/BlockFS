package com.blockfs.server;

import com.blockfs.server.models.PKBlock;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.security.*;
import java.util.BitSet;

public class BlockTest extends TestCase
{
    public BlockTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( BlockTest.class );
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

            boolean result = PKBlock.verifySignature(data.getBytes(), signature, keyPair.getPublic().getEncoded());

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
            boolean result = PKBlock.verifySignature(data.getBytes(), signature, keyPair.getPublic().getEncoded());

            assertFalse(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
