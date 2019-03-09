package com.credits.service.contract;

import com.credits.service.ServiceTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.io.File.separator;

public class ThriftIT extends ServiceTest {

    private byte[] contractState;

    public ThriftIT() {
        super("/thriftIntegrationTest/MySmartContract.java");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        contractState = deploySmartContract().newContractState;
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    @Ignore //No enough permissions
    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        executeSmartContract("balanceGet", contractState);
    }


    @Ignore
    @Test
    public void execute_contract_using_bytecode_sendTransaction() throws Exception {
        executeSmartContract("sendZeroCS", contractState);
    }

}
