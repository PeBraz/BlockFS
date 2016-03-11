package com.blockfs.client;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.Arrays;


/**
 * Unit test for simple App.
 */
public class BlockServerTest
    extends TestCase {

    private final String BLOCK_DIR = "../data";

    private final IBlockClient client = new BlockClient();


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BlockServerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(BlockServerTest.class);

    }

    @Override
    public void setUp() {
        File path = new File(BLOCK_DIR);
        if (path.exists())
            for (File f : path.listFiles())
                f.delete();
        else
            path.mkdir();
    }

    @Override
    public void tearDown() {
        File path = new File(BLOCK_DIR);
        for (File f : path.listFiles())
            f.delete();
    }


    public void testFSWriteRead1() {

        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "Hello".getBytes();
        byte[] buffer = new byte[8];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");
            client.FS_write(4, 4, data);
            client.FS_read(pkhash, 0, 8, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        byte[] expected = new byte[]{0, 0, 0, 0, 'H', 'e', 'l', 'l'};
        assertTrue(Arrays.equals(expected, buffer));
    }

    public void testFSWriteRead2() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello".getBytes();
        byte[] expected = ("1234" + "1234" + "hell" + "o").getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("1234" + "1234" + "1").getBytes();


            client.FS_write(0, initial.length, initial);
            client.FS_write(8,data.length, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertTrue(Arrays.equals(expected, buffer));

    }

    public void testFSWriteRead3() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello".getBytes();
        byte[] expected = ("12he" + "ll34" + "12").getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("1234" + "1234" + "12").getBytes();


            client.FS_write(0, initial.length, initial);
            client.FS_write(2,data.length-1, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead4() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello".getBytes();
        byte[] expected = ("1hel").getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("1234" + "1234" + "12").getBytes();


            client.FS_write(0, initial.length, initial);
            client.FS_write(1, 3, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead5() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello".getBytes();
        byte[] expected = ("1he").getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("123").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(1, 2, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead6() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello".getBytes();
        byte[] expected = ("1he").getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("12").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(1, 2, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead7() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello".getBytes();
        byte[] expected = ("1he4").getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("1234").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(1, 2, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead8() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "a".getBytes();
        byte[] expected = new byte[]{'1','2','3',0,'a'};
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("123").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(4, 1, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }
    public void testFSWriteRead9() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "a".getBytes();
        byte[] expected = new byte[]{'1','2','3','4','5', 0,'a'};
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("12345").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(6, 1, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead10() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello world".getBytes();
        byte[] expected = "he34567".getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("1234567").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(0, 2, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }
    public void testFSWriteRead11() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "hello world".getBytes();
        byte[] expected = "1234hello world4".getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("1234123412341234").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(4, 11, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException| ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    public void testFSWriteRead12() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "e".getBytes();
        byte[] expected = "1e3".getBytes();
        byte[] buffer = new byte[expected.length];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("123").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(1, 1, data);
            client.FS_read(pkhash, 0, buffer.length, buffer);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer));

    }

    /*
     * If buffer length and size are bigger than contents to read
     */
    public void testFSWriteRead13() {
        BlockClient.BLOCK_SIZE = 4;

        byte[] data = "e".getBytes();
        byte[] expected = "1e3".getBytes();
        byte[] buffer = new byte[expected.length + 2];
        try {
            String pkhash = client.FS_init(BLOCK_DIR + "/" + "joao", "password");

            byte[] initial = ("123").getBytes();

            client.FS_write(0, initial.length, initial);
            client.FS_write(1, 1, data);
            int red = client.FS_read(pkhash, 0, buffer.length, buffer);
            assertEquals(expected.length, red);
        } catch (IBlockServerRequests.IntegrityException | IBlockClient.UninitializedFSException | WrongPasswordException | ServerRespondedErrorException | ClientProblemException e) {
            fail(e.getMessage());
        }

        assertEquals(new String(expected), new String(buffer).substring(0,expected.length));

    }

}
