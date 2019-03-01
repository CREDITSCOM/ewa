package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.general.thrift.generated.APIResponse;
import org.junit.Ignore;
import org.junit.Test;

import static com.credits.TestUtils.readSourceCode;
import static org.junit.Assert.assertEquals;

public class ContractExecutorHandlerTest {

    @Test
    @Ignore("need resolve file permission for this test")
    public void compileSourceCodeTest() throws Exception {
        ContractExecutorHandler contractExecutorHandler = new ContractExecutorHandler();
        CompileSourceCodeResult sourceCodeResult =
            contractExecutorHandler.compileSourceCode(readSourceCode("com\\credits\\thrift\\MySmartContract.java"));
        assertEquals(new APIResponse((byte) 0, "success"), sourceCodeResult.status);
    }
}