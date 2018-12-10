package com.credits.service.contract;

import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.service.ServiceTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(address))).thenReturn(
            new SmartContractData(
                    address,
                    address,
                    new SmartContractDeployData(sourceCode, contractBytecode, (short)0),
                    null
            ));

        contractState = ceService.execute(address, contractBytecode, null, null, null).getContractState();
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
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
