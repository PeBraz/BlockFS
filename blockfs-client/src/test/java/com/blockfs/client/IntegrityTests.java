package com.blockfs.client;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;



public class IntegrityTests
    extends TestCase
{

    private final String BLOCK_DIR = "../data";

    private final IBlockClient client = new BlockClient();


    private final Runtime rt = Runtime.getRuntime();

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public IntegrityTests(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(IntegrityTests.class);

    }

    @Override
    public void setUp() {
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

    /**
     *  Tests that a wrong hash generated by the server is not accepted
     */
    public void testWriteDataBlockInvalid() {



        try {
            client.FS_init(BLOCK_DIR + "/joao", "1234");
        } catch (WrongPasswordException e) {
            fail();
        }
        try {
            //write will perform 1 getPKB() after finding a empty list it will put a new data block
            //the client will check a wrong hash on the put_h
            client.FS_write(0, 10, new byte[10]);
        } catch (IBlockClient.UninitializedFSException | ServerRespondedErrorException e) {
            fail();
        } catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
            return;
        }
        fail();
    }
    /**
     *  Tests that the contents of a data block received were changed, when server sends contents back to client
     */
    public void testReadDataBlockInvalid() {

        String pkHash = "";
        try {
            pkHash = client.FS_init(BLOCK_DIR + "/joao", "1234");
        } catch (WrongPasswordException e) {
            fail();
        }
        try {
            //write will perform 1 getPKB() after finding a empty list  (that shouldnt be verified)
            //the client will put a new data block
            //and put a new key block
            client.FS_write(0, 10, new byte[10]);
        } catch (IBlockClient.UninitializedFSException | IBlockServerRequests.IntegrityException | ServerRespondedErrorException e) {
            fail();
        }
        byte[] buffer = new byte[10];
        try {
            //the client will read a invalid block
            client.FS_read(pkHash, 0, 10, buffer);
        }catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
            return;
        } catch (ServerRespondedErrorException e) {
            fail();
        }

        fail();

    }
    /**
     *  Tests that the public key on a public key block was changed
     */
    public void testReadPKBlockInvalid() {


        String pkHash = "";
        try {
            pkHash = client.FS_init(BLOCK_DIR + "/joao", "1234");
        } catch (WrongPasswordException e) {
            fail();
        }
        try {
            //write will perform 1 getPKB() after finding a empty list  (that shouldnt be verified)
            //the client will put a new data block
            //and put a new key block
            client.FS_write(0, 10, new byte[10]);
        } catch (IBlockClient.UninitializedFSException | IBlockServerRequests.IntegrityException | ServerRespondedErrorException e) {
            fail();
        }
        byte[] buffer = new byte[10];
        try {
            //the client will read pk block with the wrong key
            client.FS_read(pkHash, 0, 10, buffer);
        }catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
            return;
        } catch (ServerRespondedErrorException e) {
            fail();
        }

        fail("Public key returned was invalid, but it wasn't checked");

    }

    /**
     * Tests that the signature was incorrect and server doesn't accept content (400)
     */
    public void testReadPKBInvalidSignatureAtServer() {



        try {
            client.FS_init(BLOCK_DIR + "/joao", "1234");
        } catch (WrongPasswordException e) {
            fail();
        }
        try {
            //write will perform 1 getPKB() after finding a empty list it will put a new data block
            //after the client ends writing the blocks it will update the pk block and receive a 400 from the server
            client.FS_write(0, 10, new byte[10]);
        } catch (IBlockClient.UninitializedFSException  e) {
            fail();
        } catch (IBlockServerRequests.IntegrityException | ServerRespondedErrorException e) {
            assertTrue(true);
            return;
        }
        fail();
    }

    /**
     * Tests that file signature returned to the client is incorrect
     */
    public void testReadPKBInvalidSignatureAtClient() {


        try {
            client.FS_init(BLOCK_DIR + "/joao", "1234");
        } catch (WrongPasswordException e) {
            fail();
        }
        try {
            //write will perform 1 getPKB() after finding a empty list it will put a new data block
            //after the client ends writing the blocks it will update the pk block
            // and check that the signature doesn't correspond to the data
            client.FS_write(0, 10, new byte[10]);
        } catch (IBlockClient.UninitializedFSException | ServerRespondedErrorException e) {
            fail();
        } catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
            return;
        }
        fail("Signature was incorrect, but wasn't caught");
    }


}
