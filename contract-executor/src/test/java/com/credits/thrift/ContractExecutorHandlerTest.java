package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.general.thrift.generated.APIResponse;
import org.junit.Test;

import static com.credits.TestUtils.readSourceCode;
import static org.junit.Assert.assertEquals;

public class ContractExecutorHandlerTest {

    @Test
    public void compileSourceCodeTest() throws Exception {
        ContractExecutorHandler contractExecutorHandler = new ContractExecutorHandler();
        CompileSourceCodeResult sourceCodeResult =
            contractExecutorHandler.compileSourceCode(readSourceCode("com\\credits\\thrift\\Contract.java"));
        assertEquals(new APIResponse((byte) 0, "Success"), sourceCodeResult.status);
    }
}