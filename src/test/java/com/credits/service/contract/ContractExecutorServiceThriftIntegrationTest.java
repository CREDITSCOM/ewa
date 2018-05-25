package com.credits.service.contract;

import com.credits.common.exception.CreditsException;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.credits.classload.ByteArrayContractClassLoaderTest.SimpleInMemoryCompilator.compile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractExecutorServiceThriftIntegrationTest extends ServiceTest {
    @Resource
    private ContractExecutorService ceService;

    @Resource
    private UserCodeStorageService userCodeService;

    private final String address = "1a2b3c";

    @Before
    public void setUp() throws ContractExecutorException {
        clean(address);
    }

    @Test
    public void executionTest() throws ContractExecutorException {
        String fileName = "ContractExecutorServiceThriftIntegrationTestCode.java";
        File testFile = new File(fileName);
        try (InputStream stream = getClass().getClassLoader()
            .getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            FileUtils.copyToFile(stream, testFile);
            userCodeService.store(testFile, address);
            testFile.delete();
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }

        ceService.execute(address, "");
        ceService.execute(address, "foo", null);
    }

    @Test
    public void executeByteCodeTest() throws Exception {
        String sourceCode = "public class Contract {\n" + "\n" + "    public Contract() {\n" +
            "        System.out.println(\"Hello World!!\"); \n" +
            "    }\npublic void foo(){\nSystem.out.println(\"Method foo executed\");\n}\n}";
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");
        String hashState = encrypt(bytecode);

        ApiClient mockClient = mock(ApiClient.class);
        Whitebox.setInternalState(ceService,"ldbClient", mockClient);

        when(mockClient.getSmartContract(address)).thenReturn(new SmartContractData(sourceCode, bytecode, hashState));
        ceService.execute(address, bytecode, "foo", new String[0]);

        when(mockClient.getSmartContract(address)).thenReturn(new SmartContractData(sourceCode, bytecode, "bad hash"));
        try {
            ceService.execute(address, bytecode, "foo", new String[0]);
        }catch (Exception e) {
            System.out.println("bad hash error - " + e.getMessage());
        }
    }

    private static String encrypt(byte[] bytes) throws CreditsException, NoSuchAlgorithmException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CreditsException(e);
        }
        digest.update(bytes);
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private Object[] castValues(Class<?>[] types, String[] params) throws ContractExecutorException {
        if (params == null || params.length != types.length) {
            throw new ContractExecutorException("Not enough arguments passed");
        }

        Object[] retVal = new Object[types.length];
        int i = 0;
        String param;
        Class<?> componentType;
        for (Class<?> type : types) {
            param = params[i];
            componentType = type;
            if (type.isArray()) {
                if (types.length > 1) {
                    throw new ContractExecutorException("Having array with other parameter types is not supported");
                }
                componentType = type.getComponentType();
            }

            MethodParamValueRecognizer recognizer = MethodParamValueRecognizerFactory.get(param);
            try {
                retVal[i] = recognizer.castValue(componentType);
            } catch (ContractExecutorException e) {
                throw new ContractExecutorException(
                    "Failed when casting the parameter given with the number: " + (i + 1), e);
            }
            i++;
        }

        return retVal;
    }
}
