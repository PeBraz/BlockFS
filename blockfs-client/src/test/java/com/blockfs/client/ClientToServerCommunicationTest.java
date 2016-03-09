package com.blockfs.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Unit test for simple App.
 */
public class ClientToServerCommunicationTest
    extends TestCase
{

    private final String BLOCK_DIR = "data";

//    private final IBlockServer server = new BlockFSService();


    //Test Client, calls server functions directly
    private final IBlockClient client = new BlockClient(new BlockServerRequests());


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ClientToServerCommunicationTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(ClientToServerCommunicationTest.class);
        /*
        TestSuite suite = new TestSuite();
        suite.addTest(new BlockServerTest("testFSWriteFirstBlock"));
        // .. suite.addTest(new BlockServerTest(   ....      ));
        return suite;
*/    }

    @Override
    public void setUp() {
        File path = new File(BLOCK_DIR);
        if (path.exists())
            path.delete();

        path.mkdir();
    }

    @Override
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
            String key = client.FS_init(BLOCK_DIR + "/" + "joao", "password");


            BlockServerRequests s = new BlockServerRequests();
            String originalText = "ola o meu nome é Joao";
            byte[] dataBytes = originalText.getBytes();


            String idHash = s.put_h(dataBytes);
            System.out.println("Ficheiro inserido no servidor!" + idHash);

            byte[] returnedValue = s.get("DATA"+idHash);
            String stringValue = new String(returnedValue);
            System.out.println("Ficheiro retornado do servidor!" + stringValue);
//            assertTrue(true);
            assertEquals(originalText, stringValue);
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        } catch (WrongPasswordException e) {
            e.printStackTrace();
        }

    }

    public void testPKBlock() {

        BlockClient client = new BlockClient();
        String idHash = "";
        byte[] returnedValue;
        //we create a data block
        try {
            String key = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

        BlockServerRequests s = new BlockServerRequests();
        String originalText = "ola o meu nome é Joao";
        byte[] dataBytes = originalText.getBytes();

            idHash = s.put_h(dataBytes);
            returnedValue = s.get("DATA"+idHash);
            String stringValue = new String(returnedValue);
            assertEquals(originalText, stringValue);
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        } catch (WrongPasswordException e) {
            e.printStackTrace();
            fail();
        }

        //we create the pk block
        List<String> hashes = new ArrayList<>();
        hashes.add(idHash);
        System.out.println("A hash inserida no PKBlock é:" + idHash);
        try {
            String pkHash = client.putPKB(hashes, client.getKeys());
            List<String> returnedHashes = client.getPKB(pkHash);

            assertEquals(1, returnedHashes.size());

            assertEquals(idHash, returnedHashes.get(0));
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        }


    }
}
