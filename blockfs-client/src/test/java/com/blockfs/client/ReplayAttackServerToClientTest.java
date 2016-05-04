package com.blockfs.client;

import com.blockfs.client.exception.*;
import com.blockfs.client.old.BlockClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.security.PublicKey;
import java.util.List;


/**
 * Unit test for simple App.
 */
public class ReplayAttackServerToClientTest
    extends TestCase
{

    private final String BLOCK_DIR = "data";
    private final CCBlockClient client = new CCBlockClient(new BlockServerRequests());
//    private final IBlockServer server = new BlockFSService();


    //Test Client, calls server functions directly


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ReplayAttackServerToClientTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(ReplayAttackServerToClientTest.class);
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
     *  Tests if wrong certificate list returned by server is valid
     * server:-WGLISTCERT
     */
    public void testWrongCertificatesReturned()
    {
        BlockClient.BLOCK_SIZE = 4;
        if(!Config.enableCardTests){
            assertTrue(true);
            return;
        }

        try {
            client.FS_init();
            List<PublicKey> keys = client.FS_list();
            //deve lançar excepção em cima
            fail();
        } catch (IBlockServerRequests.IntegrityException | NoCardDetectedException   | ServerRespondedErrorException | WrongPasswordException | ClientProblemException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }  catch ( InvalidCertificate e) {
            assertTrue(true);
        }

    }

    /**
     *  Tests server to client replay attack is detected
     * server:-WGREPLAYATTACK
     */
    public void testReplayAttackFromServer()
    {
        if(!Config.enableCardTests){
            assertTrue(true);
            return;
        }
        BlockClient.BLOCK_SIZE = 4;
        byte[] data = "Hello".getBytes();
        try {
            client.FS_init();
            client.FS_write(4, 4, data);
            //deve lançar excepção em cima
            fail();
        } catch (ICCBlockClient.UninitializedFSException | ClientProblemException | WrongCardPINException | IBlockServerRequests.IntegrityException | NoCardDetectedException | WrongPasswordException   e) {
            fail(e.getMessage());
        }  catch (ServerRespondedErrorException e) {
            if(e.getMessage().startsWith("replay attack")){
                assertTrue(true);
            }else
                fail(e.getMessage());
        }

    }



}
