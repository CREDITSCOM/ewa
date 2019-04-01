package com.credits.service;

import com.credits.classload.ByteCodeContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import com.credits.general.util.variant.VariantConverter;
import com.credits.pojo.ExternalSmartContract;
import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.session.DeployContractSession;
import com.credits.service.contract.session.InvokeMethodSession;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import com.credits.thrift.ReturnValue;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.general.util.Utils.getClassType;
import static java.io.File.separator;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class ServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(ServiceTest.class);

    private final String sourCodePath;

    protected final byte[] initiatorAddress = decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe");
    protected final byte[] contractAddress = decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ");
    protected List<ByteCodeObjectData> byteCodeObjectDataList;
    protected String sourceCode;
    private final ByteCodeContractClassLoader byteCodeContractClassLoader = new ByteCodeContractClassLoader();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Inject
    protected ContractExecutorService ceService;

    @Mock
    protected NodeApiExecInteractionService mockNodeApiExecService;

    public ServiceTest(String sourceCodePath) {
        this.sourCodePath = sourceCodePath;
    }

    @Before
    public void setUp() throws Exception {
        DaggerTestComponent.builder().testModule(new TestModule()).build().inject(this);

        sourceCode = readSourceCode(sourCodePath);
        byteCodeObjectDataList = compileSourceCode(sourceCode);

        ceService = spy(ceService);
        initSmartContractStaticField(null, "nodeApiService", mockNodeApiExecService);
        initSmartContractStaticField(null, "contractExecutorService", ceService);
        when(ceService.getSmartContractClassLoader()).thenReturn(byteCodeContractClassLoader);
    }

    private void initSmartContractStaticField(Object smartContractInstance, String fieldName, Object value)
        throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> contract = Class.forName("SmartContract");
        Field interactionService = contract.getDeclaredField(fieldName);
        interactionService.setAccessible(true);
        interactionService.set(smartContractInstance, value);
    }

    @After
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
            List<Diagnostic<? extends JavaFileObject>> diagnostics = compilationPackage.getCollector().getDiagnostics();
            diagnostics.forEach(action -> {
                logger.info(String.format("\nLine number: %s; Error message: %s", action.getLineNumber(), action.getMessage(null)));
            });
            throw new ContractExecutorException("Cannot compile sourceCode");
        }
    }


    private String readSourceCode(String resourcePath) throws IOException {
        String sourceCodePath = String.format("%s/src/test/resources/com/credits/service/usercode/%s", Paths.get("").toAbsolutePath(), resourcePath);
        return new String(Files.readAllBytes(Paths.get(sourceCodePath)));
    }

    protected ReturnValue executeSmartContract(String methodName, byte[] contractState) throws Exception {
        return executeSmartContract(methodName, new Variant[][] {{}}, contractState);
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

        return ceService.executeExternalSmartContract(new InvokeMethodSession(
            0,
            encodeToBASE58(initiatorAddress),
            encodeToBASE58(contractAddress),
            byteCodeObjectDataList,
            contractState,
            methodName,
            variantParams,
            Long.MAX_VALUE), usedContracts);
    }

    protected ReturnValue executeSmartContract(
        String methodName,
        Variant[][] params,
        byte[] contractState) throws Exception {

        return ceService.executeSmartContract(new InvokeMethodSession(
            0,
            encodeToBASE58(initiatorAddress),
            encodeToBASE58(contractAddress),
            byteCodeObjectDataList,
            contractState,
            methodName,
            params,
            Long.MAX_VALUE));
    }


    protected ReturnValue deploySmartContract() throws Exception {
        return ceService.deploySmartContract(new DeployContractSession(
            0,
            encodeToBASE58(initiatorAddress),
            encodeToBASE58(contractAddress),
            byteCodeObjectDataList,
            Long.MAX_VALUE));
    }

    protected void configureGetContractByteCodeNodeResponse(byte[] contractState, boolean isCanModify) {
        when(mockNodeApiExecService.getExternalSmartContractByteCode(anyLong(), anyString()))
            .thenReturn(new SmartContractGetResultData(
                new ApiResponseData(SUCCESS, "success"),
                byteCodeObjectDataList,
                contractState,
                isCanModify));
    }
}
