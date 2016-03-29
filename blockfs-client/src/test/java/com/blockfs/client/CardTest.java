package com.blockfs.client;

import com.blockfs.client.exception.NoCardDetectedException;
import com.blockfs.client.exception.WrongCardPINException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.security.PublicKey;
import java.security.cert.X509Certificate;


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
            X509Certificate cert = CardReaderClient.getCertificateFromCard();

            assertTrue(cert instanceof X509Certificate);
        }catch(NoCardDetectedException e){
            System.out.println("No card Detected. Insert the card and repeat.");
            fail();

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

            X509Certificate cert = CardReaderClient.getCertificateFromCard();
            PublicKey pubKey = cert.getPublicKey();
            byte[] signature = CardReaderClient.signWithCard(data);

            if(CryptoUtil.verifySignature(data, signature, pubKey.getEncoded())){
                System.out.println("Assinatura funcionou correctamente");
                assertTrue(true);
            }else {
                System.out.println("Assinatura nao funcionou");
                fail();
            }
        }catch(NoCardDetectedException e){
            System.out.println("No card Detected. Insert the card and repeat.");
            fail();

        }
        catch(WrongCardPINException e){
            System.out.println(e.getMessage());
            fail();

        }
        catch(Exception e){
            e.printStackTrace();
            fail();
        }

    }


}
