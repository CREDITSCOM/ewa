package com.credits.service.contract;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import org.junit.Before;
import org.junit.Test;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static com.credits.TestUtils.encrypt;
import static org.powermock.api.mockito.PowerMockito.when;

public class ThriftIntegrationTest extends ServiceTest {

    private byte[] contractBytecode;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String sourceCode = readSourceCode("/thriftIntegrationTest/Contract.java");

        contractBytecode = compile(sourceCode, "Contract", "TKN");

        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(sourceCode, contractBytecode, encrypt(contractBytecode)));
    }

    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        ceService.execute(address, contractBytecode, "balanceGet", new String[0]);
    }

    @Test
    public void execute_contract_using_bytecode_sendTransaction() throws Exception {
        ceService.execute(address, contractBytecode, "sendZeroCS", new String[0]);
    }

}
