package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.compiler.model.CompilationUnit;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.TestUtils.readSourceCode;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.compiler.InMemoryCompiler.compileSourceCode;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ContractExecutorHandlerTest {

    private static String contractSourcecode;
    private static List<ByteCodeObject> byteCodeObjects;
    private static ByteBuffer initiatorAddress;
    private static ByteBuffer contractAddress;
    private static long accessId;


    @BeforeClass
    public static void init() throws IOException, CompilationErrorException {
        contractSourcecode = readSourceCode("com\\credits\\thrift\\Contract.java");

        List<CompilationUnit> compilationUnits = compileSourceCode(contractSourcecode).getUnits();
        byteCodeObjects = compilationUnits.stream()
            .map(cu -> new ByteCodeObject(cu.getName(), ByteBuffer.wrap(cu.getByteCode())))
            .collect(Collectors.toList());

        initiatorAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"));
        contractAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd"));
        accessId = 0;
    }

    @Test
    public void compileSourceCodeTest() throws Exception {
        ContractExecutorHandler contractExecutorHandler = new ContractExecutorHandler();
        CompileSourceCodeResult sourceCodeResult = contractExecutorHandler.compileSourceCode(contractSourcecode);
        assertEquals(new APIResponse((byte) 0, "success"), sourceCodeResult.status);

    }

    @Test
    public void executeByteCodeTest() {
        ExecuteByteCodeResult executeByteCodeResult =
            new ContractExecutorHandler().executeByteCode(
                accessId,
                initiatorAddress,
                contractAddress,
                byteCodeObjects,
                ByteBuffer.wrap(new byte[] {}),
                "",
                singletonList(new Variant()), 100);

        assertEquals(new APIResponse((byte) 0, "success"), executeByteCodeResult.status);
    }
}