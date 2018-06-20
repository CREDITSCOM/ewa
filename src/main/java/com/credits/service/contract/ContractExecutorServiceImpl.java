package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.common.exception.CreditsException;
import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.secure.Sandbox;
import com.credits.serialise.Serializer;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.service.usercode.UserCodeStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.List;
import java.util.PropertyPermission;
import java.util.stream.Collectors;

import static com.credits.serialise.Serializer.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Component
public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);

    @Value("${api.server.host}")
    private String apiServerHost;

    @Value("${api.server.port}")
    private Integer apiServerPort;

    private ApiClient ldbClient;

    @Resource
    private LevelDbInteractionService dbInteractionService;


    @PostConstruct
    private void setUp() {
        ldbClient = ApiClient.getInstance(apiServerHost, apiServerPort);
        try {
            Class<?> contract = Class.forName("SmartContract");
            Field interactionService = contract.getDeclaredField("service");
            interactionService.setAccessible(true);
            interactionService.set(null, dbInteractionService);
            ldbClient = ApiClient.getInstance(apiServerHost, apiServerPort);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot load smart contract's super class", e);
        }
    }

    @Override
    public byte[] execute(String address, byte[] bytecode, byte[] contractState, String methodName, String[] params)
        throws ContractExecutorException {

        ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();

        Class<?> clazz;
        try {
            validateBytecode(address, bytecode);
            clazz = classLoader.buildClass(bytecode);
        } catch (Exception e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e));
        }

        Object instance;
        if (contractState != null && contractState.length != 0) {
            instance = deserialize(contractState, classLoader);
        } else {
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ContractExecutorException(
                    "Cannot create new instance of the contract: " + address + ". Reason: " + getRootCauseMessage(e),
                    e);
            }
        }

        List<Method> methods = Arrays.stream(clazz.getMethods()).filter(method -> {
            if (params == null || params.length == 0) {
                return method.getName().equals(methodName) && method.getParameterCount() == 0;
            } else {
                return method.getName().equals(methodName) && method.getParameterCount() == params.length;
            }
        }).collect(Collectors.toList());

        Method targetMethod = null;
        Object[] argValues = null;
        if (methods.isEmpty()) {
            throw new ContractExecutorException("Cannot execute the contract: " + address +
                ". Reason: Cannot find a method by name and parameters specified");
        } else {
            for (Method method : methods) {
                try {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length > 0) {
                        argValues = castValues(types, params);
                    }
                } catch (ClassCastException e) {
                    continue;
                } catch (ContractExecutorException e) {
                    throw new ContractExecutorException(
                        "Cannot execute the contract: " + address + "Reason: " + getRootCauseMessage(e), e);
                }
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            throw new ContractExecutorException("Cannot execute the contract: " + address +
                ". Reason: Cannot cast parameters to the method found by name: " + methodName);
        }

        //Invoking target method
        try {
            Sandbox.confine(clazz, createPermissions());
            targetMethod.invoke(instance, argValues);
        } catch (Exception e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e));
        }
        return serialize(address, instance);
    }

    private Permissions createPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new ReflectPermission("suppressAccessChecks"));
        permissions.add(new SocketPermission(apiServerHost + ":" + apiServerPort, "connect,listen,resolve"));
        permissions.add(new NetPermission("getProxySelector"));
        permissions.add(new RuntimePermission("readFileDescriptor"));
        permissions.add(new RuntimePermission("writeFileDescriptor"));
        permissions.add(new RuntimePermission("accessDeclaredMembers"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.ec"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.rsa"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.provider"));
        permissions.add(new RuntimePermission("java.lang.RuntimePermission", "loadLibrary.sunec"));
        permissions.add(new SecurityPermission("getProperty.networkaddress.cache.ttl", "read"));
        permissions.add(new SecurityPermission("getProperty.networkaddress.cache.negative.ttl", "read"));
        permissions.add(new SecurityPermission("getProperty.jdk.jar.disabledAlgorithms"));
        permissions.add(new SecurityPermission("putProviderProperty.SunRsaSign"));
        permissions.add(new SecurityPermission("putProviderProperty.SUN"));
        permissions.add(new PropertyPermission("sun.net.inetaddr.ttl", "read"));
        permissions.add(new PropertyPermission("socksProxyHost", "read"));
        permissions.add(new PropertyPermission("java.net.useSystemProxies", "read"));
        permissions.add(new PropertyPermission("java.home", "read"));
        permissions.add(new PropertyPermission("com.sun.security.preserveOldDCEncoding", "read"));
        permissions.add(new PropertyPermission("sun.security.key.serial.interop", "read"));
        permissions.add(new PropertyPermission("sun.security.rsa.restrictRSAExponent", "read"));
        return permissions;
    }

    private void validateBytecode(String address, byte[] bytecode) throws ContractExecutorException, CreditsException {
        SmartContractData smartContractData;
        try {
            smartContractData = ldbClient.getSmartContract(address);
        } catch (Exception e) {
            throw new ContractExecutorException(e.getMessage());
        }
        if (smartContractData.getHashState() != null && !smartContractData.getHashState().equals(encrypt(bytecode))) {
            throw new ContractExecutorException("unknown contract");
        }
    }

    private static String encrypt(byte[] bytes) throws CreditsException {
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
