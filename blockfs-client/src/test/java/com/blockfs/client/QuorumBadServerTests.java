package com.blockfs.client;

import com.blockfs.client.connection.ConnectionPool;
import com.blockfs.client.exception.*;
import com.blockfs.client.rest.model.PKData;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class QuorumBadServerTests
{

    private final String BLOCK_DIR = "../data";

    private final Runtime rt = Runtime.getRuntime();

    private final CCBlockClient client = new CCBlockClient();

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
     *  Tests a timeout in server pkblock
     *  server: run run_multiple_pk_timeout.bat
     */

    @Test
    public void testTimeoutPKBlock() {

        List<String> nodes = new ArrayList<String>(Config.ENDPOINTS.keySet());

        ConnectionPool pool = new ConnectionPool(nodes);


        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keys = keygen.generateKeyPair();

            String hash = pool.writeCBlock("texto".getBytes());

            List<String> hashes = new ArrayList<>();
            hashes.add(hash);
            byte[] signature;
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keys.getPrivate());

            int sequence = 0;
            PKData hashAndSequence = new PKData(sequence, hashes);
            Gson gson = new Gson();
            byte[] data = gson.toJson(hashAndSequence).getBytes();
            sig.update(data);
            signature = sig.sign();

            pool.writePK(data, signature, keys.getPublic().getEncoded());
            fail();
        }  catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            fail();
        } catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
        }
    }


    /**
     *  Tests a timeout in server content block
     *  server: run run_multiple_cb_timeout.bat
     */

    @Test
    public void testTimeoutDataBlock() {

        List<String> nodes = new ArrayList<String>(Config.ENDPOINTS.keySet());

        ConnectionPool pool = new ConnectionPool(nodes);

        try {

            String hash = pool.writeCBlock("texto".getBytes());

            fail();
        } catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
        }
    }

    /**
     *  Tests successful PBBlock read but with wrong data returned
     *  server: run run_multiple_old_pk.bat
     */

    @Test
    public void testReadPKBlock() {

        try {

            client.FS_init("joao", "password");

            byte[] data = "hello".getBytes();
            byte[] data2 = "olleh".getBytes();
            byte[] buffer = new byte[data.length];

            client.FS_write(0, data.length, data);


            client.FS_read(client.getPubKey(), 0, buffer.length, buffer);
            assertEquals(new String(data), new String(buffer));

            client.FS_write(0, data2.length, data2);
            client.FS_read(client.getPubKey(), 0, buffer.length, buffer);
            assertEquals(new String(data2), new String(buffer));
            System.out.println("Received:"+new String(buffer));


        } catch (NoCardDetectedException | WrongCardPINException | ICCBlockClient.UninitializedFSException |ServerRespondedErrorException | WrongPasswordException | ClientProblemException | IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
            fail();
        }

    }


    /**
     *  Tests Bad HMAC returned from server
     *  server: run run_multiple_read_bad_hmac.bat
     */

    @Test
    public void testHMACWithSession() {

        try {

            client.FS_init("joaosession", "password");

            byte[] data = "hello".getBytes();
            byte[] buffer = new byte[data.length];

            client.FS_write(0, data.length, data);
            client.FS_read(client.getPubKey(), 0, buffer.length, buffer);
            client.FS_read(client.getPubKey(), 0, buffer.length, buffer);

            fail();

        } catch (NoCardDetectedException | ICCBlockClient.UninitializedFSException | WrongCardPINException  | WrongPasswordException | ClientProblemException | IBlockServerRequests.IntegrityException e) {
            fail();
        } catch (ServerRespondedErrorException | NoQuorumException e) {
            assertTrue(true);
        }


    }



}
