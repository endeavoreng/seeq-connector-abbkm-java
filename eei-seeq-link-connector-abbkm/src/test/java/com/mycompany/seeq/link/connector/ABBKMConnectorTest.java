package com.mycompany.seeq.link.connector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This unit test class is included to give you a starting point for any tests you may want to write as you develop your
 * connector.
 */
public class ABBKMConnectorTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *         name of the test case
     */
    public ABBKMConnectorTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ABBKMConnectorTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }
}
