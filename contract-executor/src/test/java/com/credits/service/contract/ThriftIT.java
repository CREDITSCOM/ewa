package com.credits.service.contract;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TokenStandartData;
import com.credits.general.pojo.ByteCodeObjectData;
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
import java.util.List;

import static java.io.File.separator;
import static org.mockito.Mockito.when;

public class ThriftIT extends ServiceTest {

    private List<ByteCodeObjectData> byteCodeObjects;
    private byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String sourceCode = readSourceCode("/thriftIntegrationTest/Contract.java");

        byteCodeObjects = compileSourceCode(sourceCode);

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(initiatorAddress))).thenReturn(
            new SmartContractData(
                    initiatorAddress,
                    initiatorAddress,
                    new SmartContractDeployData(sourceCode, byteCodeObjects, TokenStandartData.CreditsBasic),
                    null
            ));

        contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjects, null, null, null, 500L).getContractState();
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    @Ignore //No enough permissions
    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjects, contractState, "balanceGet", new Variant[][]{{}},500L);
    }


    @Ignore
    @Test
    public void execute_contract_using_bytecode_sendTransaction() throws Exception {
        ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjects, contractState, "sendZeroCS", new Variant[][]{},500L);
    }

}
