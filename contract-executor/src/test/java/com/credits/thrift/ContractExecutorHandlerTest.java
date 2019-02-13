package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.api.NodeApiInteractionService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.TestUtils.readSourceCode;
import static com.credits.general.thrift.generated.Variant._Fields.V_BYTE;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.compiler.InMemoryCompiler.compileSourceCode;
import static java.util.Collections.EMPTY_LIST;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractExecutorHandlerTest {

    private static String contractSourcecode;
    private static List<ByteCodeObject> byteCodeObjects;
    private static ByteBuffer initiatorAddress;
    private static ByteBuffer contractAddress;

    private ContractExecutorHandler contractExecutorHandler;
    private NodeApiInteractionService mockApiInteractionService;

    @BeforeClass
    public static void init() throws IOException, CompilationErrorException {
        contractSourcecode = readSourceCode("com\\credits\\thrift\\Contract.java");

        List<CompilationUnit> compilationUnits = compileSourceCode(contractSourcecode).getUnits();
        byteCodeObjects = compilationUnits.stream()
            .map(cu -> new ByteCodeObject(cu.getName(), ByteBuffer.wrap(cu.getByteCode())))
            .collect(Collectors.toList());

        initiatorAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"));
        contractAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getSeedCallIntoSmartContract() {
        ExecuteByteCodeResult executeByteCodeResult = contractExecutorHandler.executeByteCode(
            0,
            initiatorAddress,
            contractAddress,
            byteCodeObjects,
            ByteBuffer.allocate(0),
            "",
            EMPTY_LIST,
            500);

        assertEquals(new APIResponse((byte) 0, "success"), executeByteCodeResult.status);

        ByteBuffer contractState = ByteBuffer.wrap(executeByteCodeResult.getContractState());

        when(mockApiInteractionService.getSeed(anyLong())).thenReturn(new byte[]{0xB, 0xA, 0xB, 0xE});

        executeByteCodeResult = contractExecutorHandler.executeByteCode(
            1,
            initiatorAddress,
            contractAddress,
            byteCodeObjects,
            contractState,
            "testGetSeed",
            EMPTY_LIST,
            5000);

        assertEquals(Arrays.asList(
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xA),
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xE)), executeByteCodeResult.getRet_val().getV_array());
    }

    @Before
    public void setUp(){
        mockApiInteractionService = mock(NodeApiInteractionService.class);
        contractExecutorHandler = new ContractExecutorHandler();
        contractExecutorHandler.service = new ContractExecutorServiceImpl(mockApiInteractionService);
    }

    @Test
    public void compileSourceCodeTest() throws Exception {
        CompileSourceCodeResult sourceCodeResult = contractExecutorHandler.compileSourceCode(contractSourcecode);
        assertEquals(new APIResponse((byte) 0, "success"), sourceCodeResult.status);

    }
}