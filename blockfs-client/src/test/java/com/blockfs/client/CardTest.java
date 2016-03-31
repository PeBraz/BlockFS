package com.blockfs.client;

import com.blockfs.client.exception.*;
import com.blockfs.client.rest.RestClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.File;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;


/**
 * Unit test for simple App.
 */
public class CardTest
    extends TestCase
{

    private final String BLOCK_DIR = "data";

//    private final IBlockServer server = new BlockFSService();


    //Test Client, calls server functions directly
    private final CCBlockClient client = new CCBlockClient(new BlockServerRequests());


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

    /**
     * Initializes the client on the server and writes a new DataBlock and PKBlock (should not see any exceptions)
     */
    public void testWriteToServer()
    {

        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "Hello".getBytes();
        try {
            client.FS_init();
            client.FS_write(4, 4, data);
            assertTrue(true);
        } catch (IBlockServerRequests.IntegrityException | ICCBlockClient.UninitializedFSException | ServerRespondedErrorException | ClientProblemException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }  catch (NoCardDetectedException | WrongCardPINException e) {
            e.printStackTrace();
            fail();
        }
    }


    /**
     * Initializes the client on the server and writes a new DataBlock and PKBlock (should not see any exceptions)
     */
    public void testWriteToServerComplex()
    {

        BlockClient.BLOCK_SIZE = 4;

        byte[] joao = " Joao".getBytes();

        byte[] data = "hello".getBytes();
        byte[] expected = ("1234" + "1234" + "hell" + "o").getBytes();
        byte[] buffer = new byte[expected.length];
//        byte[] buffer = new byte[16];
        try {
            byte[] initial = ("1234" + "1234" + "1").getBytes();
            client.FS_init();
            client.FS_write(0, initial.length, initial);
            client.FS_write(8,data.length, data);

            List<PublicKey> keys = client.FS_list();
            if(keys.size() == 0)
                fail();
            client.FS_read(keys.get(0), 0,  buffer.length, buffer);
            System.out.println("read:"+new String(buffer));
            assertTrue(true);
            assertTrue(Arrays.equals(expected, buffer));
        } catch (IBlockServerRequests.IntegrityException | InvalidCertificate | ICCBlockClient.UninitializedFSException | ServerRespondedErrorException | ClientProblemException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }  catch (NoCardDetectedException | WrongCardPINException e) {
            e.printStackTrace();
            fail();
        }
    }


    /**
     * Initializes the client on the server and writes a new DataBlock and PKBlock (should not see any exceptions)
     */
    public void testSendWrongCertificate()
    {

        KeyPairGenerator keygen = null;
        try {
            keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keys = keygen.generateKeyPair();
            X509Certificate cert = KeyStoreClient.generateCertificate(keys);
            RestClient.POST_certificate(cert);
        } catch (NoSuchAlgorithmException | CertIOException | SignatureException | InvalidKeyException | OperatorCreationException | NoSuchProviderException | CertificateException e) {
            fail(e.getMessage());
        } catch (ServerRespondedErrorException e) {
            assertTrue(true);
        }

    }


}
