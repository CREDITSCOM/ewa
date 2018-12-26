package com.credits.service.contract;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractExecutorUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.credits.general.thrift.generated.Variant.v_int;
import static com.credits.general.thrift.generated.Variant.v_string;

public class CompilationTest extends ServiceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/compilationTest/Contract.java");
        List<ByteCodeObjectData> byteCodeObjects =
            ContractExecutorUtils.compileSourceCode(sourceCode);

        byte[] contractState =
            ceService.execute(address, byteCodeObjects, null, null, null, 500L).getContractState();

        ReturnValue result = ceService.execute(address, byteCodeObjects, contractState, "addBalance",
            new Variant[][] {{v_string("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"), v_int(4)}}, 500L);
        ReturnValue result1 =
            ceService.execute(address, byteCodeObjects, result.getContractState(), "getUserBalance",
                new Variant[][] {{v_string("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe")}}, 500L);
    }
}
