package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.exception.UnsupportedTypeException;
import com.credits.leveldb.client.ApiClient;
import com.credits.secure.Sandbox;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.thrift.DeployReturnValue;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.generated.Variant;
import com.credits.thrift.utils.ContractUtils;
import com.credits.thrift.utils.VariantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.stream.Collectors;

import static com.credits.serialise.Serializer.deserialize;
import static com.credits.serialise.Serializer.serialize;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

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
    public ReturnValue execute(String address, byte[] bytecode, byte[] contractState, String methodName, String[] params)
        throws ContractExecutorException {

        ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();

        Class<?> clazz = classLoader.buildClass(bytecode);

        Object instance;
        if (contractState != null && contractState.length != 0) {
//            try {
//                SmartContractData smartContractData = ldbClient.getSmartContract(address);
//                ByteCodeValidator.validateBytecode(bytecode, smartContractData); TODO: uncomment this section if everything goes right
//            } catch (LevelDbClientException | CreditsNodeException e) {
//                throw new ContractExecutorException(
//                    "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e));
//            }
            instance = deserialize(contractState, classLoader);
        } else {
            DeployReturnValue deployReturnValue = ContractUtils.deploy(clazz, address);
            return new ReturnValue(deployReturnValue.getContractState(), null, deployReturnValue.getContractVariables());
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
        Object returnObject;
        Class<?> returnType = targetMethod.getReturnType();
        try {
            Sandbox.confine(clazz, createPermissions());
            returnObject = targetMethod.invoke(instance, argValues);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e));
        }

        Variant returnValue = null;
        if (returnType != void.class) {
            returnValue =
                new VariantMapper().apply(returnObject)
                    .orElseThrow(() -> {
                        UnsupportedTypeException e = new UnsupportedTypeException(
                            "Unsupported type of the value {" + returnObject.toString() + "}: " + returnObject.getClass());
                        return new ContractExecutorException(
                            "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e), e);
                    });
        }

        Map<String, Variant> contractVariables = ContractUtils.getContractVariables(instance);

        return new ReturnValue(serialize(address, instance), returnValue, contractVariables);
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
