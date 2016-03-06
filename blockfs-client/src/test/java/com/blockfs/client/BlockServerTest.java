package com.blockfs.client;

import junit.framework.TestCase;


/**
 * Unit test for simple App.
 */
public class BlockServerTest
    extends TestCase
{

//    private final String BLOCK_DIR = "data";
//
//    private final IBlockServer server = new BlockFSService();
//
//
//    //Test Client, calls server functions directly
//    private final IBlockClient client = new BlockClient(new IBlockServerRequests() {
//        @Override
//        public byte[] get(String hash) {
//            return server.get(hash);
//        }
//
//        @Override
//        public String put_k(byte[] data, byte[] signature, byte[] pubKey) {
//
//            try {
//                return server.put_k(data, signature, pubKey);
//            } catch (WrongDataSignature wrongDataSignature) {
//                wrongDataSignature.printStackTrace();
//            }
//            return "";
//        }
//
//        @Override
//        public String put_h(byte[] data) {
//            return server.put_h(data);
//        }
//
//    });
//
//
//    /**
//     * Create the test case
//     *
//     * @param testName name of the test case
//     */
//    public BlockServerTest(String testName )
//    {
//        super( testName );
//    }
//
//    /**
//     * @return the suite of tests being tested
//     */
//    public static Test suite()
//    {
//        return new TestSuite(BlockServerTest.class);
//        /*
//        TestSuite suite = new TestSuite();
//        suite.addTest(new BlockServerTest("testFSWriteFirstBlock"));
//        // .. suite.addTest(new BlockServerTest(   ....      ));
//        return suite;
//*/    }
//
//    @Override
//    public void setUp() {
//        File path = new File(BLOCK_DIR);
//        if (path.exists())
//            path.delete();
//
//        path.mkdir();
//    }
//
//    @Override
//    public void tearDown() {
//        File path = new File(BLOCK_DIR);
//        path.delete();
//    }
//
//    /**
//     * Writes a new File:
//     *  - will generate 2 files server side ( 1 data block & 1 public key block )
//     *  - will pass integrity validation
//     */
//    public void testFSWriteFirstBlock()
//    {
//        byte[] data = new byte[BlockClient.BLOCK_SIZE];
//        String key = client.FS_init();
//        try {
//            client.FS_write(0, BlockClient.BLOCK_SIZE, data);
//        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException e) {
//            fail();
//            return;
//        }
//        assertEquals(new File("data").listFiles().length, 2);
//        assertTrue(new File("data", CryptoUtil.generateHash(data)).exists());
//        assertTrue(new File("data", CryptoUtil.generateHash(((BlockClient)client).getPublic())).exists());
//    }
}
