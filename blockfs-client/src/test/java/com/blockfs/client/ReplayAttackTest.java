package com.blockfs.client;

import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.PKData;
import com.google.gson.Gson;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.security.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Unit test for simple App.
 */
public class ReplayAttackTest
    extends TestCase
{

    private final String BLOCK_DIR = "data";
    private static final String ENDPOINT = "http://0.0.0.0:5050/";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ReplayAttackTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(ReplayAttackTest.class);
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
     * Tests two consecutive posts of the same data to the server:
     */
    public void testReplayAttack()
    {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keys = keygen.generateKeyPair();

            String hash = RestClient.POST_cblock("texto".getBytes(), ENDPOINT);
            List<String> hashes = new ArrayList<>();
            hashes.add(hash);
            //PKBlock pkBlock = new PKBlock();
            byte[] signature;
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keys.getPrivate());

            int sequence = 0;
            PKData hashAndSequence = new PKData(sequence, hashes);
            Gson gson = new Gson();
            byte[] data = gson.toJson(hashAndSequence).getBytes();
            sig.update(data);
            signature = sig.sign();

            RestClient.POST_pkblock(data, signature, keys.getPublic().getEncoded(), ENDPOINT);


            //we make a replay attack
            RestClient.POST_pkblock(data, signature, keys.getPublic().getEncoded(), ENDPOINT);
            fail();



        } catch (ServerRespondedErrorException e) {
            if(e.getMessage() != null && e.getMessage().startsWith("401")){
                System.out.println("Replay attack detected");
                assertTrue(true);
            }else{
                fail();
            }

        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            fail();
        }
    }


    /**
     * Tests send an old PKBlock to the server
     */
    public void testReplayAttackComplex()
    {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keys = keygen.generateKeyPair();

            String hash = RestClient.POST_cblock("texto".getBytes(), ENDPOINT);
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

            RestClient.POST_pkblock(data, signature, keys.getPublic().getEncoded(), ENDPOINT);


            hash = RestClient.POST_cblock("texto2".getBytes(), ENDPOINT);
            hashes.add(hash);
            hash = RestClient.POST_cblock("texto3".getBytes(), ENDPOINT);
            hashes.add(hash);


            //new PKBlock
            byte[] signatureCurrent;
            Signature sigCurrent = Signature.getInstance("SHA256withRSA");
            sigCurrent.initSign(keys.getPrivate());
            int sequenceCurrent = 1;
            PKData hashAndSequenceCurrent = new PKData(sequenceCurrent, hashes);
            byte[] dataCurrent = gson.toJson(hashAndSequenceCurrent).getBytes();
            sigCurrent.update(dataCurrent);
            signatureCurrent = sigCurrent.sign();
            RestClient.POST_pkblock(dataCurrent, signatureCurrent, keys.getPublic().getEncoded(), ENDPOINT);



            //we make a replay attack with old values
            RestClient.POST_pkblock(data, signature, keys.getPublic().getEncoded(), ENDPOINT);
            fail();



        } catch (ServerRespondedErrorException e) {
            if(e.getMessage() != null && e.getMessage().startsWith("401")){
                System.out.println("Replay attack detected");
                assertTrue(true);
            }else{
                fail();
            }

        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            fail();
        }
    }



}
