package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.credits.TestUtils.readSourceCode;
import static com.credits.general.thrift.generated.Variant._Fields.V_BYTE;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.compiler.InMemoryCompiler.compileSourceCode;
import static java.util.Collections.EMPTY_LIST;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("unable access to FS")
public class ContractExecutorHandlerTest {

    private static String contractSourcecode;
    private static List<ByteCodeObject> byteCodeObjects;
    private static ByteBuffer initiatorAddress;
    private static ByteBuffer contractAddress;

    private ContractExecutorHandler contractExecutorHandler;
    private NodeApiExecInteractionService mockApiExecInteractionService;
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @BeforeClass
    public static void init() throws IOException, CompilationErrorException {
        contractSourcecode = readSourceCode("com\\credits\\thrift\\MySmartContract.java");

        List<CompilationUnit> compilationUnits = compileSourceCode(contractSourcecode).getUnits();
        byteCodeObjects = compilationUnits.stream()
            .map(cu -> new ByteCodeObject(cu.getName(), ByteBuffer.wrap(cu.getByteCode())))
            .collect(Collectors.toList());

        initiatorAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"));
        contractAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd"));
    }

    @Before
    public void setUp(){
        mockApiExecInteractionService = mock(NodeApiExecInteractionService.class);
        contractExecutorHandler = new ContractExecutorHandler();
        contractExecutorHandler.service = new ContractExecutorServiceImpl(mockApiExecInteractionService);
    }

    @Test
    public void getSeedCallIntoSmartContract() throws Exception {
        ExecuteByteCodeResult executeByteCodeResult = deploySmartContract();
        assertEquals(new APIResponse((byte) 0, "success"), executeByteCodeResult.status);

        when(mockApiExecInteractionService.getSeed(anyLong())).thenReturn(new byte[]{0xB, 0xA, 0xB, 0xE});
        executeByteCodeResult = executeSmartContract(ByteBuffer.wrap(executeByteCodeResult.getInvokedContractState()), 1, "testGetSeed", 5000);
        assertEquals(Arrays.asList(
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xA),
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xE)), executeByteCodeResult.getRet_val().getV_array());
    }



    @Test
    public void useContractProperties() throws Exception {
        ExecuteByteCodeResult executeByteCodeResult = deploySmartContract();
        executeByteCodeResult = executeSmartContract(ByteBuffer.wrap(executeByteCodeResult.getInvokedContractState()), 1234, "getProperties", 1000);
        assertEquals("1234 5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe 5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd", executeByteCodeResult.getRet_val().getV_string());

    }


    @Test(timeout = 8000)
    public void parallelCallLongTimeExecutionContract() throws Exception {
        ExecuteByteCodeResult executeByteCodeResult = deploySmartContract();
        CompletableFuture.allOf(
            runAsyncContractCall(1, executeByteCodeResult.invokedContractState, "waitFiveSec"),
            runAsyncContractCall(2, executeByteCodeResult.invokedContractState, "waitFiveSec"),
            runAsyncContractCall(3, executeByteCodeResult.invokedContractState, "waitFiveSec"),
            runAsyncContractCall(4, executeByteCodeResult.invokedContractState, "waitFiveSec")).join();
    }

    private CompletableFuture runAsyncContractCall(int accessId, ByteBuffer contractState, String methodName) {
        return runAsync(() -> {
            try {
                executeSmartContract(contractState, accessId, methodName, 10_000);
            } catch (TException e) {
                System.out.println(e.getMessage());
            }
        }, cachedThreadPool);
    }

    @Test
    public void compileSourceCodeTest() throws Exception {
        CompileSourceCodeResult sourceCodeResult = contractExecutorHandler.compileSourceCode(contractSourcecode,(byte) 100);
        assertEquals(new APIResponse((byte) 0, "success"), sourceCodeResult.status);

    }

    @SuppressWarnings("unchecked")
    private ExecuteByteCodeResult executeSmartContract(ByteBuffer contractState, int accessId, String methodName, int executiontime)
        throws TException {
        SmartContractBinary smartContractBinary = new SmartContractBinary(contractAddress,byteCodeObjects,contractState, true);
        return contractExecutorHandler.executeByteCode(accessId, initiatorAddress, smartContractBinary, methodName, EMPTY_LIST, executiontime,
            (byte) 100);
    }

    private ExecuteByteCodeResult deploySmartContract() throws TException {
        return executeSmartContract(ByteBuffer.allocate(0), 1123, "", 500);
    }
}
