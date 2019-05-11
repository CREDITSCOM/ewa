package tests.credits.thrift;

import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.MethodHeader;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.thrift.ContractExecutorHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.executor.ContractExecutorService;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.ApplicationProperties.APP_VERSION;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.compiler.InMemoryCompiler.compileSourceCode;
import static java.nio.ByteBuffer.allocate;
import static java.nio.ByteBuffer.wrap;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static tests.credits.TestUtils.readSourceCode;

public class ContractExecutorHandlerTest {

    private static String contractSourcecode;
    private static List<ByteCodeObject> byteCodeObjects;
    private static ByteBuffer initiatorAddress;
    private static ByteBuffer contractAddress;

    @Inject
    ContractExecutorService mockCEService;
    @Inject
    ContractExecutorHandler contractExecutorHandler;

    @BeforeAll
    public static void init() throws IOException, CompilationException {
        contractSourcecode = readSourceCode("com/credits/thrift/MySmartContract.java");

        List<CompilationUnit> compilationUnits = compileSourceCode(contractSourcecode).getUnits();
        byteCodeObjects = compilationUnits.stream()
                .map(cu -> new ByteCodeObject(cu.getName(), wrap(cu.getByteCode())))
                .collect(Collectors.toList());

        initiatorAddress = wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"));
        contractAddress = wrap(decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd"));
    }

    @BeforeEach
    public void setUp() {
        DaggerCEHandlerTestComponent.builder().build().inject(this);
    }

    @Test
    @DisplayName("MethodHeaders can be null when deploy")
    public void test0() {
        var deployResult = executeSmartContract(allocate(0), null);
        verify(mockCEService).deploySmartContract(any());
        assertTrue(deployResult.results.get(0).getInvokedContractState().length > 0);
    }

    @Test
    @DisplayName("MethodHeaders can be empty when deploy")
    public void test1() {
        var deployResult = executeSmartContract(allocate(0), emptyList());
        assertTrue(deployResult.results.get(0).getInvokedContractState().length > 0);
    }

    @Test
    @DisplayName("MethodHeaders can't be null when execute")
    public void test2() {
        var contractState = deploySmartContract();
        assertThrows(IllegalArgumentException.class, () -> executeSmartContract(contractState, null));

    }

    @Test
    @DisplayName("MethodHeaders can't be empty when execute")
    public void test3() {
        var contractState = deploySmartContract();
        assertThrows(IllegalArgumentException.class, () -> executeSmartContract(contractState, emptyList()));
    }

    @Test
    @DisplayName("Only first MethodHeader can be null")
    public void test4() {
        var executeResult = executeSmartContract(allocate(0), emptyList());
        assertTrue(executeResult.results.get(0).getInvokedContractState().length > 0);
    }

    @Test
    @DisplayName("Throw exception if version not valid")
    public void test5() {

    }

    @Test
    private ExecuteByteCodeResult executeSmartContract(ByteBuffer contractState, List<MethodHeader> methodHeaders) {
        return executeSmartContract(contractState, methodHeaders, APP_VERSION);
    }

    @Test
    private ExecuteByteCodeResult executeSmartContract(ByteBuffer contractState, List<MethodHeader> methodHeaders, short version) {
        SmartContractBinary smartContractBinary = new SmartContractBinary(contractAddress, new ClassObject(byteCodeObjects, contractState), true);
        return contractExecutorHandler.executeByteCode(1, initiatorAddress, smartContractBinary, methodHeaders, 500, version);
    }


    private ByteBuffer deploySmartContract() {
        return wrap(executeSmartContract(allocate(0), emptyList()).getResults().get(0).getInvokedContractState());
    }
}
