package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.test.init.TestState;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartContractControllerTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SmartContractControllerTest.class);

    private String sourceCode = "public class Contract extends SmartContract \n" +
            "{ \n" +
            "\tpublic Contract() {\n" +
            "\t\tSystem.out.println(\"Constructor\"); \n" +
            "\t} \n" +
            "\tpublic void initialize() { } \n" +
            "\tpublic void balanceGet() throws Exception { \n" +
            "\t\tSystem.out.println(\"getBalance()\"); \n" +
            //"\t\tjava.math.BigDecimal balance = getBalance(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"CS\"); \n" +
            //"\t\tSystem.out.println(\"getBalance = \" + balance); \n" +
            "\t} \n" +
            "\tpublic void sendZeroCS() throws Exception {\n" +
            "\t\tSystem.out.println(\"try to send 0 credits...\"); sendTransaction(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", 0, \"CS\"); System.out.println(\"success\"); \n" +
            "\t} \n" +
            "}";

    private String sourceCode02 =  "public class Contract extends SmartContract { public Contract() { System.out.println(\"Constructor\"); } public void initialize() { } public void balanceGet() throws Exception { System.out.println(\"getBalance()\"); java.math.BigDecimal balance = getBalance(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"CS\"); System.out.println(\"getBalance = \" + balance); } public void sendZeroCS() throws Exception { System.out.println(\"try to send 0 credits...\"); sendTransaction(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", 0, \"CS\"); System.out.println(\"success\"); } }";

    @Before
    public void init() {
        TestState.init();
    }
}
