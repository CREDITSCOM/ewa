package com.credits.service.contract;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static com.credits.TestUtils.encrypt;
import static java.io.File.separator;
import static org.mockito.Mockito.when;

public class ThriftIT extends ServiceTest {

    private byte[] contractBytecode;
    private byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String sourceCode = readSourceCode("/thriftIntegrationTest/Contract.java");

        contractBytecode = compile(sourceCode, "Contract", "TKN");

        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(address,sourceCode, contractBytecode,null, encrypt(contractBytecode),"",new ArrayList<>()));

        contractState = ceService.execute(address, contractBytecode, null, null, null).getContractState();
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }

    @Ignore //No enough permissions
    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        ceService.execute(address, contractBytecode, contractState, "balanceGet", new String[0]);
    }


    @Ignore
    @Test
    public void execute_contract_using_bytecode_sendTransaction() throws Exception {
        ceService.execute(address, contractBytecode, contractState, "sendZeroCS", new String[0]);
    }

}
