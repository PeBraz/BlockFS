package com.blockfs.client;

import com.blockfs.server.BlockFSService;
import com.blockfs.server.IBlockServer;
import com.blockfs.server.exceptions.WrongDataSignature;
import com.blockfs.server.utils.CryptoUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Unit test for simple App.
 */
public class BlockServerTest
    extends TestCase
{

    private final String BLOCK_DIR = "data";

    private final IBlockServer server = new BlockFSService();

    //Test Client, calls server functions directly
    private final IBlockClient client = new BlockClient(new IBlockServerRequests() {
        @Override
        public byte[] get(String hash) {

             return server.get(hash);
        }

        @Override
        public String put_k(byte[] data, byte[] signature, byte[] pubKey) {
            try {
                return server.put_k(data, signature, pubKey);
            } catch (WrongDataSignature wrongDataSignature) {
                wrongDataSignature.printStackTrace();
            }
            return "";
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
        return new TestSuite(BlockServerTest.class);

    }

    @Override
    public void setUp() {
        BlockClient.BLOCK_SIZE = 4;
        File path = new File(BLOCK_DIR);
        if (path.exists())
            for (File f : path.listFiles())
                f.delete();
        else
            path.mkdir();
    }

    @Override
    public void tearDown() {
        File path = new File(BLOCK_DIR);
        for (File f : path.listFiles())
            f.delete();
    }

    public void testPutH(){

        byte[] data = "Hello world!".getBytes();

        //creates a block with length data.length and returns the hash
        String hash = server.put_h(data);

        //hash returned is correct
        assertEquals(hash, CryptoUtil.generateHash(data));


        Path p = Paths.get(BLOCK_DIR, hash);
        //file was created locally in the server
        assertTrue(Files.exists(p));

        try {
            byte[] content = Files.readAllBytes(p);
            //file content is the same as the original
            assertEquals(new String(content), new String(data));
        } catch (IOException e) {
            fail();
        }
    }

    public void testGetH() {

        byte[] data = "Hello world!".getBytes();

        byte[] content = server.get(server.put_h(data));

        assertEquals(new String(data), new String(content));

    }


    /**
     * Writes a new File:
     *  - will generate 2 files server side ( 1 data block & 1 public key block )
     *  - will pass integrity validation
     *  - doesn't check content
     */
    public void testFSWriteFirstBlock()
    {


        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "Hello".getBytes();
        byte[] buffer = new byte[8];
        String pkhash = client.FS_init();

        try {
            client.FS_write(4, 4, data);
            client.FS_read(pkhash, 4, 4, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException e) {
            fail();
        }
        assertEquals(new String(new byte[] {0,0,0,0,'H', 'e','l', 'l', 'o'}), new String(buffer));
    }
    /**
     *  Writes a new file with 3 blocks
     *      - blocks 1 and 2 are the same, pkb should point twice to the same block
     */

    public void testFSWriteBlocks()
    {
        byte[] data = new byte[2050]; //3 blocks

        String pkFilename = client.FS_init();
        try {
            client.FS_write(0, data.length, data);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException e) {
            fail();
        }

        String data1Filename = CryptoUtil.generateHash(new byte[1024]); //data block 2 is the same block (same hash)
        String data3Filename = CryptoUtil.generateHash(new byte[2]);

        //check if files were created successfully
        assertEquals(new File("data").listFiles().length, 3);
        assertTrue("data block1 " + data1Filename + " should exist ", new File("data", data1Filename).exists());
        assertTrue("data block3 " + data1Filename + " should exist ", new File("data", data3Filename).exists());
        assertTrue("Public key block " + pkFilename + " should exist", new File("data", pkFilename).exists());

        //checks Public key file contents (locally at server side)
        // (Won't work, because locally file is stored as json)
        /*Path p = Paths.get("data", pkFilename);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Files.readAllBytes(p)))){
            List<String> hashes = (List<String>)ois.readObject();
            assertEquals(hashes, Arrays.asList( new String[] {data1Filename,data1Filename,data3Filename}));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            fail();
        }*/

        // checks data block contents
        Path p1 = Paths.get("data", data1Filename);
        Path p3 = Paths.get("data", data3Filename);
        try {
            byte[] content = Files.readAllBytes(p1);
            assertEquals(new String(content), new String(data));
            content = Files.readAllBytes(p3);
            assertEquals(new String(content), new String(new byte[2]));
        } catch (IOException e) {
            fail();
        }

    }

    /**
     *  Writes a new file with 3 blocks
     *      - blocks 1 and 2 are the same, pkb should point twice to the same block
     */
    public void testFSWriteOverBlocks()
    {
        byte[] old_data = new byte[2050]; //3 blocks
        byte[] new_data = "Hello World!".getBytes();
        String pkFilename = client.FS_init();
        try {
            client.FS_write(0, old_data.length, old_data);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException e) {
            fail();
        }

        try {
            client.FS_write(1020, new_data.length, new_data);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException e) {
            fail();
        }


        /*


        String data1Filename = CryptoUtil.generateHash(new byte[1024]); //data block 2 is the same block (same hash)
        String data3Filename = CryptoUtil.generateHash(new byte[2]);

        //check if files were created successfully
        assertEquals(new File("data").listFiles().length, 3);
        assertTrue("data block1 " + data1Filename + " should exist ", new File("data", data1Filename).exists());
        assertTrue("data block3 " + data1Filename + " should exist ", new File("data", data3Filename).exists());
        assertTrue("Public key block " + pkFilename + " should exist", new File("data", pkFilename).exists());
*/
        //checks Public key file contents (locally at server side)
        // (Won't work, because locally file is stored as json)
        /*Path p = Paths.get("data", pkFilename);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Files.readAllBytes(p)))){
            List<String> hashes = (List<String>)ois.readObject();
            assertEquals(hashes, Arrays.asList( new String[] {data1Filename,data1Filename,data3Filename}));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            fail();
        }*/

        // checks data block contents
      /*  Path p1 = Paths.get("data", data1Filename);
        Path p3 = Paths.get("data", data3Filename);
        try {
            byte[] content = Files.readAllBytes(p1);
            assertEquals(new String(content), new String(data));
            content = Files.readAllBytes(p3);
            assertEquals(new String(content), new String(new byte[2]));
        } catch (IOException e) {
            fail();
        }
*/
    }

/*
    public void testFSRead() {

        String data = "Hello world!";
        byte[] buffer = new byte[BlockClient.BLOCK_SIZE];
        String key = client.FS_init();
        try {
            client.FS_write(0, data.length(), data.getBytes());   //create my file
            client.FS_read(key, 0, data.length(), buffer); //read my file
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException e) {
            fail();
        }
        assertEquals(data, new String(buffer));
    }*/
}
