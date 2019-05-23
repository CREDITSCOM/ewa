package tests.credits.service;

import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import com.credits.general.util.variant.VariantConverter;
import com.credits.scapi.v0.SmartContract;
import exception.ContractExecutorException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.ExternalSmartContract;
import pojo.ReturnValue;
import pojo.apiexec.SmartContractGetResultData;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;
import tests.credits.DaggerTestComponent;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.general.util.Utils.getClassType;
import static java.io.File.separator;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public abstract class ServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(ServiceTest.class);

    private final String sourCodePath;


    protected final String initiatorAddressBase58 = "5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe";
    protected final String contractAddressBase58 = "G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ";
    protected final byte[] initiatorAddress = decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe");
    protected final byte[] contractAddress = decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ");
    protected List<ByteCodeObjectData> byteCodeObjectDataList;
    protected String sourceCode;
    final protected long accessId = 0;
    private final ByteCodeContractClassLoader byteCodeContractClassLoader = new ByteCodeContractClassLoader();
    private DeployContractSession deployContractSession;

    @Inject
    protected ContractExecutorService ceService;

    protected NodeApiExecInteractionService mockNodeApiExecService;

    public ServiceTest(String sourceCodePath) {
        this.sourCodePath = sourceCodePath;
    }

    @BeforeEach
    public void setUp() throws Exception {
        DaggerTestComponent.builder().build().inject(this);

        sourceCode = readSourceCode(sourCodePath);
        byteCodeObjectDataList = compileSourceCode(sourceCode);

        ceService = spy(ceService);
        mockNodeApiExecService = mock(NodeApiExecInteractionService.class);
        initSmartContractStaticField(null, "nodeApiService", mockNodeApiExecService);
        initSmartContractStaticField(null, "contractExecutorService", ceService);
        when(ceService.getSmartContractClassLoader()).thenReturn(byteCodeContractClassLoader);

        deployContractSession = new DeployContractSession(
                0,
                encodeToBASE58(initiatorAddress),
                encodeToBASE58(contractAddress),
                byteCodeObjectDataList,
                Long.MAX_VALUE);
    }

    private void initSmartContractStaticField(Object smartContractInstance,
                                              String fieldName,
                                              Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> contract = SmartContract.class;
        Field interactionService = contract.getDeclaredField(fieldName);
        interactionService.setAccessible(true);
        interactionService.set(smartContractInstance, value);
    }

    @AfterEach
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    private static List<ByteCodeObjectData> compileSourceCode(String sourceCode) {
        Map<String, String> classesToCompile = new HashMap<>();
        String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
        classesToCompile.put(className, sourceCode);

        CompilationPackage compilationPackage = new InMemoryCompiler().compile(classesToCompile);
        if (compilationPackage.isCompilationStatusSuccess()) {
            return GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
        } else {
            var errors = compilationPackage.getCollector().getDiagnostics().stream()
                    .map(e -> "Line number: " + e.getLineNumber() + " Error message:" + e.getMessage(null))
                    .collect(Collectors.joining("\n"));

            logger.error(errors);
            throw new ContractExecutorException("Cannot compile sourceCode. \n" + errors);
        }
    }


    private String readSourceCode(String resourcePath) throws IOException {
        String sourceCodePath = String.format("%s/src/test/resources/com/credits/service/usercode/%s", Paths.get("").toAbsolutePath(), resourcePath);
        return new String(Files.readAllBytes(Paths.get(sourceCodePath)));
    }

    protected ReturnValue executeExternalSmartContract(String methodName, byte[] contractState, Object... params) {
        Variant[][] variantParams = null;
        if (params != null) {
            variantParams = new Variant[1][params.length];
            for (int i = 0; i < variantParams[0].length; i++) {
                final Object param = params[i];
                variantParams[0][i] = VariantConverter.toVariant(getClassType(param), param);
            }
        }
        Map<String, ExternalSmartContract> usedContracts = new HashMap<>();
        usedContracts.putIfAbsent(
                encodeToBASE58(contractAddress),
                new ExternalSmartContract(new SmartContractGetResultData(
                        new ApiResponseData(SUCCESS, ""),
                        byteCodeObjectDataList,
                        contractState,
                        true)));

        return ceService.executeExternalSmartContract(
                createMethodSession(methodName, contractState, Long.MAX_VALUE, variantParams),
                usedContracts,
                byteCodeContractClassLoader);
    }

    protected ReturnValue executeSmartContract(String methodName, byte[] contractState) {
        return executeSmartContract(methodName, new Variant[][]{{}}, contractState);
    }

    protected ReturnValue executeSmartContract(String methodName, Variant[][] params, byte[] contractState) {
        return executeSmartContract(methodName, params, contractState, Long.MAX_VALUE);
    }

    protected ReturnValue executeSmartContract(String methodName,  byte[] contractState, long executionTime) {
        return executeSmartContract(methodName, new Variant[][]{{}}, contractState, executionTime);
    }

    protected ReturnValue executeSmartContract(
            String methodName,
            Variant[][] params,
            byte[] contractState,
            long executionTime) {
        return ceService.executeSmartContract(createMethodSession(methodName, contractState, executionTime, params));
    }

    protected ReturnValue deploySmartContract() {
        return ceService.deploySmartContract(deployContractSession);
    }

    protected void configureGetContractByteCodeNodeResponse(byte[] contractState, boolean isCanModify) {
        when(mockNodeApiExecService.getExternalSmartContractByteCode(anyLong(), anyString()))
                .thenReturn(new SmartContractGetResultData(
                        new ApiResponseData(SUCCESS, "success"),
                        byteCodeObjectDataList,
                        contractState,
                        isCanModify));
    }

    private InvokeMethodSession createMethodSession(String methodName, byte[] contractState, long maxValue, Variant[][] variantParams) {
        return new InvokeMethodSession(
                0,
                encodeToBASE58(initiatorAddress),
                encodeToBASE58(contractAddress),
                byteCodeObjectDataList,
                contractState,
                methodName,
                variantParams,
                maxValue);
    }
}
