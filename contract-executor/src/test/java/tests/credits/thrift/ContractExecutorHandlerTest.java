package tests.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.MethodHeader;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.secure.PermissionsManager;
import com.credits.thrift.ContractExecutorHandler;
import org.apache.thrift.TException;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.node.NodeApiExecInteractionService;
import tests.credits.DaggerTestComponent;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.credits.general.thrift.generated.Variant._Fields.V_BYTE;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.compiler.InMemoryCompiler.compileSourceCode;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tests.credits.TestUtils.readSourceCode;

@Ignore("not access to fs")
public class ContractExecutorHandlerTest {

    private static String contractSourcecode;
    private static List<ByteCodeObject> byteCodeObjects;
    private static ByteBuffer initiatorAddress;
    private static ByteBuffer contractAddress;

    @Inject
    PermissionsManager permissionsManager;

    private ContractExecutorHandler contractExecutorHandler;
    private NodeApiExecInteractionService mockApiExecInteractionService;
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @BeforeAll
    public static void init() throws IOException, CompilationException {
        contractSourcecode = readSourceCode("com\\credits\\thrift\\MySmartContract.java");

        List<CompilationUnit> compilationUnits = compileSourceCode(contractSourcecode).getUnits();
        byteCodeObjects = compilationUnits.stream()
                .map(cu -> new ByteCodeObject(cu.getName(), ByteBuffer.wrap(cu.getByteCode())))
                .collect(Collectors.toList());

        initiatorAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"));
        contractAddress = ByteBuffer.wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd"));
    }

    @BeforeEach
    public void setUp() {
        DaggerTestComponent.builder().build().inject(this);
        mockApiExecInteractionService = mock(NodeApiExecInteractionService.class);
        contractExecutorHandler = new ContractExecutorHandler();
    }

    @Test
    @DisplayName("getSeed must be return expectedValue")
    public void getSeedCallIntoSmartContract() throws Exception {
        var expectedValue = new byte[]{0xB, 0xA, 0xB, 0xE};

        ExecuteByteCodeResult executeByteCodeResult = deploySmartContract();
        assertEquals(new APIResponse((byte) 0, "success"), executeByteCodeResult.status);

        when(mockApiExecInteractionService.getSeed(anyLong())).thenReturn(expectedValue);
        executeByteCodeResult = executeSmartContract(ByteBuffer.wrap(executeByteCodeResult.getResults().get(0).getInvokedContractState()), 1, "testGetSeed", 5000);
        assertEquals(Arrays.asList(
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xA),
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xE)), executeByteCodeResult.getResults().get(0).getRet_val().getV_array());
    }


    @Test
    @DisplayName("constructor must be have access to global variables like sessionID, initiator, ...")
    public void useContractProperties() throws Exception {
        ExecuteByteCodeResult executeByteCodeResult = deploySmartContract();
        executeByteCodeResult = executeSmartContract(ByteBuffer.wrap(executeByteCodeResult.getResults().get(0).getInvokedContractState()), 1234, "getProperties", 1000);
        assertEquals("1234 5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe 5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd", executeByteCodeResult.getResults().get(0).getRet_val().getV_string());
    }


    @Test
    public void compileSourceCodeTest() throws Exception {
        CompileSourceCodeResult sourceCodeResult = contractExecutorHandler.compileSourceCode(contractSourcecode, (byte) 100);
        assertEquals(new APIResponse((byte) 0, "success"), sourceCodeResult.status);
    }

    @SuppressWarnings("unchecked")
    private ExecuteByteCodeResult executeSmartContract(ByteBuffer contractState, int accessId, String methodName, int executiontime) {
        SmartContractBinary smartContractBinary = new SmartContractBinary(contractAddress, new ClassObject(byteCodeObjects, contractState), true);
        return contractExecutorHandler.executeByteCode(accessId, initiatorAddress, smartContractBinary, singletonList(new MethodHeader(methodName, EMPTY_LIST)), executiontime, ((short) 100));
    }

    private ExecuteByteCodeResult deploySmartContract() throws TException {
        return executeSmartContract(ByteBuffer.allocate(0), 1123, "", 500);
    }
}
