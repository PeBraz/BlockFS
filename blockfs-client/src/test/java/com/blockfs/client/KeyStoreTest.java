package com.blockfs.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;


/**
 * Unit test for simple App.
 */
public class KeyStoreTest
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
    public KeyStoreTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(KeyStoreTest.class);
    }

    @Override
    public void setUp() {
        File path = new File( BLOCK_DIR);
        if (path.exists())
            for (File f : path.listFiles())
                f.delete();
        else
            path.mkdir();
    }

    @Override
    public void tearDown() {
        File path = new File( BLOCK_DIR);
        for (File f : path.listFiles())
            f.delete();
    }

    /**
     * Creates a keypair saves it to disk and loads it to check if the public key hash is the same:
     */
    public void testSaveLoadKeys()
    {
        String key1 = null;
        try {
            key1 = client.FS_init(BLOCK_DIR + "/" + "joao", "password");
            String key2 = client.FS_init(BLOCK_DIR + "/" + "joao", "password");
            assertEquals(key1, key2);

        } catch (WrongPasswordException | ClientProblemException e) {
            e.printStackTrace();
            fail();
        }


    }

    /**
     * Tests wrong password:
     */
    public void testKeyStorePassword()
    {
        try {
            String key1 = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            String key2 = null;

            key2 = client.FS_init(BLOCK_DIR + "/" + "joao", "differentpassword");

            assertEquals(key1, key2);
        } catch (WrongPasswordException e) {
            assertTrue(true);
        } catch (ClientProblemException e) {
            fail();
        }
    }

    /**
     * Tests creating different users keyPair, checks if the same user/password combination
     * creates the same hash and if different users have different keys and hashes.
     */
    public void testDifferentUsers()
    {
        String key1,key2, key3, key4 = null;
        try {
            key1 = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            key2 = client.FS_init(BLOCK_DIR + "/" + "nuno", "outrapass");

            key3 = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            key4 = client.FS_init(BLOCK_DIR + "/" + "nuno", "outrapass");

            assertEquals(key1, key3);
            assertEquals(key2, key4);
            assertNotSame(key1, key2);

        } catch (WrongPasswordException | ClientProblemException e) {
            fail();
        }


    }

}
