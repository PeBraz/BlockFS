package com.blockfs.client;

import com.blockfs.server.BlockFS;
import com.blockfs.server.IBlockServer;
import com.blockfs.server.utils.CryptoUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class BlockServerTest
    extends TestCase
{

    private final String BLOCK_DIR = "data";

    private final IBlockServer server = new BlockFS();


    //Test Client, calls server functions directly
    private final IBlockClient client = new BlockClient(new IBlockServerRequests() {
        @Override
        public byte[] get(String hash) {
            return server.get(hash);
        }

        @Override
        public String put_k(byte[] data, String signature, String pubKey) {
            return server.put_k(data, signature, pubKey);
        }

        @Override
        public String put_h(byte[] data) {
            return server.put_h(data);
        }

    });



    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BlockServerTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new BlockServerTest("testFSWriteFirstBlock"));
        // .. suite.addTest(new BlockServerTest(   ....      ));
        return suite;
    }


    public void setUp() {
        File path = new File(BLOCK_DIR);
        if (path.exists())
            path.delete();

        path.mkdir();
    }

    public void tearDown() {
        File path = new File(BLOCK_DIR);
        path.delete();
    }

    /**
     * Writes a new File:
     *  - will generate 2 files server side ( 1 data block & 1 public key block )
     *  - will pass integrity validation
     */
    public void testFSWriteFirstBlock()
    {
        byte[] data = new byte[BlockClient.BLOCK_SIZE];
        try {
            client.FS_write(0, BlockClient.BLOCK_SIZE, data);
        } catch (IBlockServerRequests.IntegrityException e) {
            fail();
            return;
        }

        assertEquals(new File("data").listFiles().length, 2);
        assertTrue(new File("data", CryptoUtil.generateHash(data)).exists());
        assertTrue(new File("data", ((BlockClient)client).getPublic()).exists());
    }
}
