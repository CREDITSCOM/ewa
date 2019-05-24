package tests.credits.thrift;

import com.credits.client.executor.thrift.generated.*;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.thrift.ContractExecutorHandler;
import exception.ContractExecutorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pojo.ReturnValue;
import pojo.SmartContractMethodResult;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.ApplicationProperties.APP_VERSION;
import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.thrift.generated.Variant._Fields.*;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.compiler.InMemoryCompiler.compileSourceCode;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static java.nio.ByteBuffer.allocate;
import static java.nio.ByteBuffer.wrap;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static tests.credits.TestUtils.readSourceCode;

public class ContractExecutorHandlerTest {

    private static List<ByteCodeObject> byteCodeObjects;
    private static ByteBuffer initiatorAddress;
    private static ByteBuffer contractAddress;
    private final Variant voidVariantResult = new Variant(V_VOID, VOID_TYPE_VALUE);

    @Inject
    ContractExecutorService mockCEService;
    @Inject
    ContractExecutorHandler contractExecutorHandler;
    private byte[] contractState = new byte[]{0xC, 0xA, 0xF, 0xE};
    private Variant getTokensVariantResult;
    private static String contractSourcecode;

    @BeforeAll
    public static void init() throws IOException, CompilationException {
        contractSourcecode = readSourceCode("com/credits/service/usercode/contractExecutorHandlerTest/MySmartContract.java");

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
        when(mockCEService.deploySmartContract(any())).thenReturn(
                new ReturnValue(new byte[]{0xC, 0xA, 0xF, 0xE},
                                List.of(new SmartContractMethodResult(SUCCESS_API_RESPONSE, voidVariantResult, 10)),
                                emptyMap()));
    }

    @Test
    @DisplayName("MethodHeaders can be null when deploy")
    public void executeByteCodeTest0() {
        var deployResult = executeSmartContract(allocate(0), null);
        verify(mockCEService).deploySmartContract(any());
        assertThat(deployResult.status, is(SUCCESS_API_RESPONSE));
        assertThat(deployResult.results.get(0).getInvokedContractState().length > 0, is(true));
    }

    @Test
    @DisplayName("MethodHeaders can be empty when deploy")
    public void executeByteCodeTest1() {
        var deployResult = executeSmartContract(allocate(0), emptyList());
        verify(mockCEService).deploySmartContract(any());

        assertEquals(SUCCESS_API_RESPONSE, deployResult.status);
        assertTrue(deployResult.results.get(0).getInvokedContractState().length > 0);
    }

    @Test
    @DisplayName("MethodHeaders can't be null when execute")
    public void executeByteCodeTest2() {
        var contractState = deploySmartContract();
        var executeByteCodeResult = executeSmartContract(contractState, null);
        verify(mockCEService, never()).executeSmartContract(any());


        assertThat(executeByteCodeResult.status.code, is(FAILURE.code));
        assertThat(executeByteCodeResult.results.size(), is(0));
        assertThat(executeByteCodeResult.status.getMessage(), containsString("IllegalArgumentException: method headers list can't be null or empty"));
    }

    @Test
    @DisplayName("MethodHeaders can't be empty when execute")
    public void executeByteCodeTest3() {
        var contractState = deploySmartContract();
        var executeByteCodeResult = executeSmartContract(contractState, emptyList());
        verify(mockCEService, never()).executeSmartContract(any());

        assertThat(executeByteCodeResult.status.code, is(FAILURE.code));
        assertThat(executeByteCodeResult.results.size(), is(0));
        assertThat(executeByteCodeResult.status.getMessage(), containsString("IllegalArgumentException: method headers list can't be null or empty"));
    }

    @Test
    @DisplayName("Amount ExecuteByteCodeResult must be equals amount methodHeaders")
    public void executeByteCodeTest5() {
        var contractState = deploySmartContract();

        getTokensVariantResult = new Variant(V_INT, 5);

        doAnswer(invocationOnMock -> {
            var methodName = ((InvokeMethodSession) invocationOnMock.getArgument(0)).methodName;
            switch (methodName) {
                case "getTokens":
                    return createSuccessResponse(getTokensVariantResult);
                case "addTokens":
                    return createSuccessResponse(voidVariantResult);
                default:
                    throw new ContractExecutorException("unknown method");
            }
        }).when(mockCEService).executeSmartContract(any());

        var executeByteCodeResult = executeSmartContract(contractState, List.of(new MethodHeader("getTokens", emptyList()),
                                                                                new MethodHeader("addTokens", List.of(getTokensVariantResult)),
                                                                                new MethodHeader("unknown", emptyList()),
                                                                                new MethodHeader("getTokens", emptyList())));

        assertThat(executeByteCodeResult.status, is(SUCCESS_API_RESPONSE));
        assertThat(executeByteCodeResult.results.size(), is(4));

        List<SetterMethodResult> results = executeByteCodeResult.getResults();

        assertThat(results.get(0).status, is(SUCCESS_API_RESPONSE));
        assertThat(results.get(0).ret_val, is(getTokensVariantResult));

        assertThat(results.get(1).status, is(SUCCESS_API_RESPONSE));
        assertThat(results.get(1).ret_val, is(voidVariantResult));

        assertThat(results.get(2).status.code, is(FAILURE.code));
        assertThat(results.get(2).status.message, containsString("ContractExecutorException: unknown method"));
        assertThat(results.get(2).ret_val, is(new Variant(V_STRING, "unknown method")));

        assertThat(results.get(3).status, is(SUCCESS_API_RESPONSE));
        assertThat(results.get(3).ret_val, is(getTokensVariantResult));
    }

    @Test
    @DisplayName("Throw exception if version is not valid")
    @SuppressWarnings("unchecked")
    public void checkAppVersionTest() {
        short invalidVersion = (short) (APP_VERSION - 1);
        var contractState = deploySmartContract();

        assertVersionIsInvalid(executeSmartContract(contractState, List.of(new MethodHeader("getTokens", emptyList())), invalidVersion).getStatus());
        assertVersionIsInvalid(executeByteCodeMultiple(contractState, "getTokens", invalidVersion, emptyList()).getStatus());
        assertVersionIsInvalid(contractExecutorHandler.compileSourceCode(contractSourcecode, invalidVersion).getStatus());
        assertVersionIsInvalid(contractExecutorHandler.getContractMethods(List.of(mock(ByteCodeObject.class)), invalidVersion).getStatus());
        assertVersionIsInvalid(contractExecutorHandler.getContractVariables(List.of(mock(ByteCodeObject.class)),
                                                                            contractState,
                                                                            invalidVersion).getStatus());
    }

    private void assertVersionIsInvalid(APIResponse status) {
        assertThat(status.code, is(FAILURE.code));
        assertThat(status.getMessage(), containsString("IllegalArgumentException: Invalid version"));
    }

    private ReturnValue createSuccessResponse(Variant result) {
        return new ReturnValue(
                contractState,
                List.of(new SmartContractMethodResult(SUCCESS_API_RESPONSE, result, 10)),
                emptyMap());
    }

    private ExecuteByteCodeResult executeSmartContract(ByteBuffer contractState, List<MethodHeader> methodHeaders) {
        return executeSmartContract(contractState, methodHeaders, APP_VERSION);
    }

    private ExecuteByteCodeMultipleResult executeByteCodeMultiple(ByteBuffer contractState,
                                                                  String methodName,
                                                                  short version,
                                                                  List<Variant>... params) {
        return contractExecutorHandler.executeByteCodeMultiple(1,
                                                               initiatorAddress,
                                                               new SmartContractBinary(contractAddress,
                                                                                       new ClassObject(byteCodeObjects, contractState),
                                                                                       false),
                                                               methodName,
                                                               List.of(params),
                                                               Long.MAX_VALUE,
                                                               version);
    }

    private ExecuteByteCodeResult executeSmartContract(ByteBuffer contractState, List<MethodHeader> methodHeaders, int version) {
        SmartContractBinary smartContractBinary = new SmartContractBinary(contractAddress, new ClassObject(byteCodeObjects, contractState), true);
        return contractExecutorHandler.executeByteCode(1, initiatorAddress, smartContractBinary, methodHeaders, 500, (short) version);
    }


    private ByteBuffer deploySmartContract() {
        return wrap(executeSmartContract(allocate(0), emptyList()).getResults().get(0).getInvokedContractState());
    }
}
