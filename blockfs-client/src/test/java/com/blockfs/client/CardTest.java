package com.blockfs.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.security.PublicKey;


/**
 * Unit test for simple App.
 */
public class CardTest
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
    public CardTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(CardTest.class);
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
            PublicKey pubKey = KeyStoreClient.getPubKeyFromCard();

            assertTrue(pubKey instanceof PublicKey);
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }

    }


    /**
     * creates a certificate with the Cartao Cidadao :
     */
    public void testSignWithCard()
    {
        byte[] data = "uma assinatura".getBytes();
        try {
            PublicKey pubKey = KeyStoreClient.getPubKeyFromCard();

            byte[] signiture = KeyStoreClient.signWithCard(data);

            if(CryptoUtil.verifySignature(data, signiture, pubKey.getEncoded())){
                System.out.println("Assinatura funcionou correctamente");
                assertTrue(true);
            }else {
                System.out.println("Assinatura nao funcionou");
                fail();
            }

        }catch(Exception e){
            e.printStackTrace();
            fail();
        }

    }


}
