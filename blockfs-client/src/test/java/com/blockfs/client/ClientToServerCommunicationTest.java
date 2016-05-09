package com.blockfs.client;

import com.blockfs.client.old.BlockClient;
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
    private final ICCBlockClient client = new CCBlockClient(new BlockServerRequests());


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
    }

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

    public void testDataBlock()
    {
        try {
            client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            BlockServerRequests s = new BlockServerRequests();
            String originalText = "ola o meu nome é Joao";
            byte[] dataBytes = originalText.getBytes();


            String idHash = s.put_h(dataBytes);

            byte[] returnedValue = s.get("DATA"+idHash).getData();
            String stringValue = new String(returnedValue);
            assertEquals(originalText, stringValue);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
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
            returnedValue = s.get("DATA"+idHash).getData();
            String stringValue = new String(returnedValue);
            assertEquals(originalText, stringValue);
        }  catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        //we create the pk block
        List<String> hashes = new ArrayList<>();
        hashes.add(idHash);
        try {
            String pkHash = client.putPKB(hashes, client.getKeys());
            List<String> returnedHashes = client.getPKB(pkHash);

            assertEquals(1, returnedHashes.size());

            assertEquals(idHash, returnedHashes.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }


    }
}
