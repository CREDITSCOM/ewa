package com.credits.service.contract;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.service.ServiceTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CompilationTest extends ServiceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/compilationTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjects =
            compileSourceCode(sourceCode);

       //fixme
//        byte[] contractState =
//            ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjects, null, null, null, 500L).getContractState();

//        ReturnValue result = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjects, contractState, "addBalance",
//            new Variant[][] {{v_string("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"), v_int(4)}}, 500L);
//        ReturnValue result1 =
//            ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjects, result.getContractState(), "getUserBalance",
//                new Variant[][] {{v_string("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe")}}, 500L);
    }
}
