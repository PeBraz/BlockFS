package com.blockfs.client;

import com.blockfs.client.connection.ConnectionPool;
import com.blockfs.client.old.BlockClient;
import com.blockfs.client.old.IBlockClient;
import com.blockfs.client.rest.model.PKData;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class QuorumBadServerTests
{

    private final String BLOCK_DIR = "../data";

    private final IBlockClient client = new BlockClient();


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
     *  Tests a timeout in server pkblock
     *  server: run run_multiple_pk_timeout.bat
     */

    @Test
    public void testTimeoutPKBlock() {

        List<String> nodes = Arrays.asList(Config.ENDPOINTS);

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

        List<String> nodes = Arrays.asList(Config.ENDPOINTS);

        ConnectionPool pool = new ConnectionPool(nodes);

        try {

            String hash = pool.writeCBlock("texto".getBytes());

            fail();
        } catch (IBlockServerRequests.IntegrityException e) {
            assertTrue(true);
        }



    }



}
