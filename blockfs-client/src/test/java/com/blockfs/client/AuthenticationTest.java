package com.blockfs.client;

import com.blockfs.client.exception.*;
import com.blockfs.client.rest.RestClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AuthenticationTest extends TestCase
{

    public static final String SERVER1 = "http://0.0.0.0:5050/";
    public static final String SERVER2 = "http://0.0.0.0:5051/";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AuthenticationTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(AuthenticationTest.class);
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }


    /**
     *  Tests a client init
     *
     */

    public void testIncorrectHMAC() {
        try {
            RestClient.GET_certificates(SERVER1);
            RestClient.GET_certificates(SERVER2);

            fail();
        } catch (ServerRespondedErrorException e) {
            assertTrue(true);
        }


    }

}
