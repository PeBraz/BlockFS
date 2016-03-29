package com.blockfs.client;

import com.blockfs.client.rest.RestClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.List;


/**
 * Unit test for simple App.
 */
public class GsonTest
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
    public GsonTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(GsonTest.class);
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
     * Tests if the client can load the pubkey from the Cartao Cidadao:
     */
    public void testLoadPubKey()
    {

        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keys = keygen.generateKeyPair();
            X509Certificate cert = KeyStoreClient.generateCertificate(keys);
            RestClient.POST_certificate(cert);

            List<X509Certificate> certs = RestClient.GET_certificates();


            assertTrue(certs.size() > 0);

            String encodedKey1 = new String(Hex.encode(keys.getPublic().getEncoded()));
            String encodedKey2 = new String(Hex.encode(certs.get(certs.size()-1).getPublicKey().getEncoded()));
            assertEquals(encodedKey1, encodedKey2);
            if (encodedKey1.equals(encodedKey2)){
                System.out.println("received certificate match");
            }else {
                System.out.println("not equals!");
            }


        }catch(Exception e){
            e.printStackTrace();
            fail();
        }

    }




}
