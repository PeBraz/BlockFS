package com.blockfs.client;

import com.blockfs.client.exception.*;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;


public class QuorumTests extends TestCase
{

    private final String BLOCK_DIR = "../data";

    private final CCBlockClient client = new CCBlockClient();


    private final Runtime rt = Runtime.getRuntime();

    @Before
    public void setUp() {
        File path = new File(BLOCK_DIR);
        if (path.exists())
            for (File f : path.listFiles())
                f.delete();
        else
            path.mkdir();
    }

    @After
    public void tearDown() {
        File path = new File(BLOCK_DIR);
        for (File f : path.listFiles())
            f.delete();
    }

    /**
     *  Tests a client init
     *
     */

    public void testFS_Init() {

        try {
            client.FS_init( "joao", "password");
            assertTrue(true);
        } catch (NoCardDetectedException | ServerRespondedErrorException | WrongPasswordException | ClientProblemException | IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
            fail();
        }

    }

    /**
     *  Tests a client Write and Read
     *
     */

    public void testFS_WriteRead() {

        try {

            client.FS_init( "joao", "password");
            byte[] data = "hello".getBytes();
            byte[] buffer = new byte[data.length];

            client.FS_write(0, data.length, data);


            client.FS_read(client.getPubKey(), 0, buffer.length, buffer);

            assertEquals(new String(data), new String(buffer));
        } catch (NoCardDetectedException | WrongCardPINException | ICCBlockClient.UninitializedFSException |ServerRespondedErrorException | WrongPasswordException | ClientProblemException | IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
            fail();
        }

    }

    /**
     *  Tests a list pub keys
     *
     */

    public void testFS_ListPubKeys() {

        try {

            client.FS_init( "joao", "password");
            List<PublicKey> keys = client.FS_list();
            assertTrue(!keys.isEmpty());
            boolean found = false;
            byte[] mykey = client.getPubKey().getEncoded();
            for (PublicKey key : keys){
                if (Arrays.equals(key.getEncoded(), mykey)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);

        } catch (NoCardDetectedException | InvalidCertificate  |ServerRespondedErrorException |
                WrongPasswordException | ClientProblemException | IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
            fail();
        }

    }






}
