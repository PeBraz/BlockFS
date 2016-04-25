package com.blockfs.server;

import com.blockfs.server.utils.DataBlock;
import com.blockfs.server.utils.CryptoUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

public class DataBlockTest extends TestCase
{
    public DataBlockTest(String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( DataBlockTest.class );
    }

    public void testWriteBlock() {

        KeyPairGenerator keygen = null;

        try {
            keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keyPair = keygen.generateKeyPair();
            String hash = CryptoUtil.generateHash(keyPair.getPublic().getEncoded());

            Path dir = Paths.get("./data/2000");
            Files.createDirectories(dir);

            DataBlock.writeBlock("Hello Hello!".getBytes(), hash, 2000);

            Path file = Paths.get("./data/2000", hash);
            byte[] data = Files.readAllBytes(file);

            assertTrue(data.length != 0);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testReadBlock() {

        try {
            String hash = "abcdefg";

            Files.createDirectories(Paths.get("./data/2000"));
            DataBlock.writeBlock("Hello Hello!".getBytes(), hash, 2000);

            byte[] data = DataBlock.readBlock(hash, 2000);
            assertEquals("Hello Hello!", new String(data));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
