package com.credits.service.contract;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import com.credits.thrift.generated.Variant;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
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

        when(mockLevelDbService.getSmartContract(address)).thenReturn(
            new SmartContractData(address, address, sourceCode, contractBytecode,null));

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
        ceService.execute(address, contractBytecode, contractState, "balanceGet", new Variant[0]);
    }


    @Ignore
    @Test
    public void execute_contract_using_bytecode_sendTransaction() throws Exception {
        ceService.execute(address, contractBytecode, contractState, "sendZeroCS", new Variant[0]);
    }

}
