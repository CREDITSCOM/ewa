package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.common.utils.Base58;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.ApiClient;
import com.credits.secure.Sandbox;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.thrift.DeployReturnValue;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.generated.Variant;
import com.credits.thrift.utils.ContractUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
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
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot load smart contract's super class", e);
        }
    }

    @Override
    public ReturnValue execute(
            byte[] initiatorAddress,
            byte[] bytecode,
            byte[] contractState,
            String methodName,
            Variant[] params
    ) throws ContractExecutorException {

        String initiator = Base58.encode(initiatorAddress);
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
            DeployReturnValue deployReturnValue = ContractUtils.deployAndGetContractVariables(clazz, initiator);
            return new ReturnValue(deployReturnValue.getContractState(), null, deployReturnValue.getContractVariables());
        }

        initializeInitiator(initiator, clazz, instance);

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
            throw new ContractExecutorException("Cannot execute the contract: " + initiator +
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
                            "Cannot execute the contract: " + initiator + "Reason: " + getRootCauseMessage(e), e);
                }
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            throw new ContractExecutorException("Cannot execute the contract: " + initiator +
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
                    "Cannot execute the contract: " + initiator + ". Reason: " + getRootCauseMessage(e));
        }

        Variant returnValue = null;
        if (returnType != void.class) {
            returnValue = ContractUtils.mapObjectToVariant(returnObject);
        }

        Map<String, Variant> contractVariables = ContractUtils.getContractVariables(instance);

        return new ReturnValue(serialize(initiator, instance), returnValue, contractVariables);
    }

    private void initializeInitiator(String initiator, Class<?> clazz, Object instance) {
        try {
            Field initiatorField = clazz.getField("initiator");
            initiatorField.setAccessible(true);
            initiatorField.set(instance, initiator);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot initialize \"initiator\" field. Reason:" + e.getMessage());
        }
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

    private Object parseObjectFromVariant(Variant variant) throws ContractExecutorException {
//        Object value = null;
        if (variant.isSetV_string()) {
            return variant.getV_string();
        } else if (variant.isSetV_bool()) {
            return variant.getV_bool();
        } else if (variant.isSetV_double()) {
            return variant.getV_double();
        } else if (variant.isSetV_i8()) {
            return variant.getV_i8();
        } else if (variant.isSetV_i16()) {
            return variant.getV_i16();
        } else if (variant.isSetV_i32()) {
            return variant.getV_i32();
        } else if (variant.isSetV_i64()) {
            return variant.getV_i64();

        } else if (variant.isSetV_list()) {
            List<Variant> variantList = variant.getV_list();
            List objectList = new ArrayList();
            for(Variant element : variantList) {
                objectList.add(parseObjectFromVariant(element));
            }
            return objectList;
        } else if (variant.isSetV_map()) {
            Map<Variant, Variant> variantMap = variant.getV_map();
            Map objectMap = new HashMap();
            for (Map.Entry<Variant, Variant> entry : variantMap.entrySet()) {
                objectMap.put(
                        parseObjectFromVariant(entry.getKey()),
                        parseObjectFromVariant(entry.getValue())
                );
            }
            return objectMap;
        } else if (variant.isSetV_set()) {
            Set<Variant> variantSet = variant.getV_set();
            Set objectSet = new HashSet();
            for(Variant element : variantSet) {
                objectSet.add(parseObjectFromVariant(element));
            }
            return objectSet;
        }
        throw new ContractExecutorException("Unsupported variant type");
    }

    private Object[] castValues(Class<?>[] types, Variant[] params) throws ContractExecutorException {
        if (params == null || params.length != types.length) {
            throw new ContractExecutorException("Not enough arguments passed");
        }
        Object[] retVal = new Object[types.length];
        int i = 0;
        Variant param;
        for (Class<?> type : types) {
            param = params[i];
            if (type.isArray()) {
                if (types.length > 1) {
                    throw new ContractExecutorException("Having array with other parameter types is not supported");
                }
            }

            retVal[i] = parseObjectFromVariant(param);
            logger.info(String.format("param[%s] = %s", i, retVal[i]));
            i++;
        }
        return retVal;
    }
}