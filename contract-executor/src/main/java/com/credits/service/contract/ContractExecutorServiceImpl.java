package com.credits.service.contract;

import com.credits.ApplicationProperties;
import com.credits.classload.BytecodeContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.exception.CompilationException;
import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.pojo.MethodData;
import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.secure.Sandbox;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.nio.ByteBuffer;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.serialize.Serializer.deserialize;
import static com.credits.serialize.Serializer.serialize;
import static com.credits.service.contract.SmartContractConstants.initSessionSmartContractConstants;
import static com.credits.thrift.ContractExecutorHandler.ERROR_CODE;
import static com.credits.thrift.ContractExecutorHandler.SUCCESS_CODE;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.thrift.utils.ContractExecutorUtils.mapObjectToVariant;
import static com.credits.utils.ContractExecutorServiceUtils.castValues;
import static com.credits.utils.ContractExecutorServiceUtils.getMethodArgumentsValuesByNameAndParams;
import static com.credits.utils.ContractExecutorServiceUtils.initializeField;
import static com.credits.utils.ContractExecutorServiceUtils.initializeSmartContractField;
import static com.credits.utils.ContractExecutorServiceUtils.parseAnnotationData;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);
    private ExecutorService executorService;
    private final Permissions smartContractPermissions;

    @Inject
    public ApplicationProperties properties;


    public ContractExecutorServiceImpl(NodeApiExecInteractionService dbInteractionService) {
        executorService = Executors.newCachedThreadPool();
        INJECTOR.component.inject(this);
        try {
            Class<?> contract = Class.forName("SmartContract");
            initializeSmartContractField("service", dbInteractionService, contract, null);
            initializeSmartContractField("contractExecutorService", this, contract, null);
            initializeSmartContractField("cachedPool", Executors.newCachedThreadPool(), contract, null);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load smart contract's super class. Reason: ", e);
        }

        try {
            Class<?> serviceClass = Class.forName("com.credits.service.node.apiexec.NodeApiExecServiceImpl");
            Permissions permissions = createServiceApiPermissions();
            permissions.add(new SocketPermission(properties.apiHost + ":" + properties.executorNodeApiPort, "connect,listen,resolve"));
            Sandbox.confine(serviceClass, permissions);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot add permissions api service. Reason: ", e);
        }

        smartContractPermissions = createSmartContactPermissions();
    }

    @Override
    public ReturnValue deploySmartContract(Session session) {
        try {
            BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
            Class<?> contractClass = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader);

            Sandbox.confine(contractClass, smartContractPermissions);

            initSessionSmartContractConstants(
                currentThread().getId(),
                session.initiatorAddress,
                session.contractAddress,
                session.accessId);

            return new ReturnValue(
                serialize(contractClass.newInstance()),
                singletonList(new SmartContractMethodResult(new APIResponse(SUCCESS_CODE, "success"), null)), null);

        } catch (Throwable e) {
            logger.debug("Cannot invokeMethod the contract. Root cause message: {}\n{}", getRootCauseMessage(e), e);
            throw new ContractExecutorException(
                "Cannot deploy the contract " + session.contractAddress + ", accessId " + session.accessId + ". Reason: " +
                    getRootCauseMessage(e));
        }
    }

    @Override
    public ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException {

        try {
            BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
            Class<?> contractClass = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader);

            Sandbox.confine(contractClass, smartContractPermissions);

            Object instance = deserialize(session.contractState, classLoader);

            final Map<ByteBuffer, ByteBuffer> externalContractStates = new HashMap<>();
            initializeField("initiator", session.initiatorAddress, contractClass, instance);
            initializeField("accessId", session.accessId, contractClass, instance);
            initializeField("externalContracts", externalContractStates, contractClass, instance);

            //todo add "void" type to Variant
            return stream(session.paramsTable)
                .flatMap(params -> {
                    MethodData methodData = getMethodArgumentsValuesByNameAndParams(contractClass, session.methodName, params);
                    return Stream.of(invokeSmartContractMethod(session, instance, methodData));
                })
                .reduce(
                    new ReturnValue(null, new ArrayList<>(), externalContractStates),
                    (returnValue, smartContractMethodResult) -> {
                        if (returnValue.newContractState == null) {
                            returnValue.newContractState = serialize(instance);
                        }
                        returnValue.executeResults.add(smartContractMethodResult);
                        return returnValue;
                    },
                    (returnValue, returnValue2) -> returnValue);

        } catch (Throwable e) {
            logger.debug(
                "Cannot executeSmartContract the contract. Root cause message: {}\n",
                getRootCauseMessage(e),
                e);
            throw new ContractExecutorException(
                "Cannot execute the contract " + session.contractAddress + ", accessId " + session.accessId +
                    ". Reason: " + getRootCauseMessage(e));
        }
    }


    @Override
    public List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");

        BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
        Class<?> contractClass =
            compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
        Set<String> objectMethods = new HashSet<>(asList(
            "getClass",
            "hashCode",
            "equals",
            "toString",
            "notify",
            "notifyAll",
            "wait",
            "finalize"));
        List<MethodDescriptionData> result = new ArrayList<>();
        for (Method method : contractClass.getMethods()) {
            if (objectMethods.contains(method.getName())) {
                continue;
            }
            ArrayList<MethodArgumentData> args = new ArrayList<>();
            List<AnnotationData> methodAnnotationDataList = new ArrayList<>();
            for (Annotation annotation : method.getAnnotations()) {
                AnnotationData methodAnnotationData = parseAnnotationData(annotation.toString());
                methodAnnotationDataList.add(methodAnnotationData);
            }
            for (Parameter parameter : method.getParameters()) {
                List<AnnotationData> paramAnnotationDataList = new ArrayList<>();
                for (Annotation annotation : parameter.getAnnotations()) {
                    AnnotationData parameterAnnotationData = parseAnnotationData(annotation.toString());
                    paramAnnotationDataList.add(parameterAnnotationData);
                }
                args.add(new MethodArgumentData(parameter.getType().getTypeName(), parameter.getName(),
                                                paramAnnotationDataList));
            }
            result.add(new MethodDescriptionData(method.getGenericReturnType().getTypeName(), method.getName(), args,
                                                 methodAnnotationDataList));
        }

        return result;
    }

    @Override
    public Map<String, Variant> getContractVariables(List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState)
        throws ContractExecutorException {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");
        requireNonNull(contractState, "contract state is null");

        if (contractState.length != 0) {
            BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
            Class<?> contractClass =
                compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
            return ContractExecutorUtils.getContractVariables(deserialize(contractState, classLoader));
        } else {
            throw new ContractExecutorException("contract state is empty");
        }
    }

    @Override
    public List<ByteCodeObjectData> compileClass(String sourceCode)
        throws ContractExecutorException, CompilationErrorException, CompilationException {
        requireNonNull(sourceCode, "sourceCode of contract class is null");
        if (sourceCode.isEmpty()) {
            throw new ContractExecutorException("sourceCode of contract class is empty");
        }
        CompilationPackage compilationPackage = InMemoryCompiler.compileSourceCode(sourceCode);
        return GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
    }

    @Override
    public ReturnValue executeExternalSmartContract(
        long accessId, String initiatorAddress,
        String externalSmartContractAddress, String externalSmartContractMethod,
        List<Object> externalSmartContractParams, SmartContractGetResultData externalSmartContractByteCode,
        Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode) {
        //
        //        List<ByteCodeObjectData> byteCodeObjectDataList = externalSmartContractByteCode.getByteCodeObjects();
        //        byte[] contractState = externalSmartContractByteCode.getContractState();
        //
        //        Variant[] params = new Variant[externalSmartContractParams.size()];
        //        for (int i = 0; i < externalSmartContractParams.size(); i++) {
        //            Variant variant = mapObjectToVariant(externalSmartContractParams.get(i));
        //            params[i] = variant;
        //        }
        //
        //
        //        try {
        //            if (byteCodeObjectDataList.size() == 0) {
        //                throw new ContractExecutorException("Bytecode size is 0");
        //            }
        //            requireNonNull(initiatorAddress, "initiatorAddress is null");
        //            requireNonNull(externalSmartContractAddress, "contractAddress is null");
        //
        //            BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
        //            Class<?> contractClass =
        //                compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
        //            initializeField("accessId", accessId, contractClass, null);
        //            initializeField("initiator", initiatorAddress, contractClass, null);
        //            initializeField("contractAddress", externalSmartContractAddress, contractClass,
        //                            null);
        //
        //
        //            Object instance;
        //            if (contractState != null && contractState.length != 0) {
        //                instance = deserialize(contractState, classLoader);
        //                initializeField("initiator", initiatorAddress, contractClass, instance);
        //            } else {
        //                instance = contractClass.newInstance();
        //                return new ReturnValue(serialize(instance), null, null,
        //                                       singletonList(new APIResponse(SUCCESS_CODE, "success")));
        //            }
        //
        //            initializeField("externalContracts", externalContractsStateByteCode,
        //                            contractClass, null);
        //            initializeField("contractAddress", externalSmartContractAddress, contractClass,
        //                            instance);
        //            initializeField("accessId", accessId, contractClass, instance);
        //
        //            int amountParamRows = 1;
        //
        //            MethodData targetMethodData =
        //                getMethodArgumentsValuesByNameAndParams(contractClass, externalSmartContractMethod, params);
        //
        //            VariantData[] returnVariantDataList = new VariantData[amountParamRows];
        //            APIResponse[] returnStatuses = new APIResponse[amountParamRows];
        //
        //            Class<?> returnType = targetMethodData.getMethod().getReturnType();
        //
        //            for (int i = 0; i < amountParamRows; i++) {
        //                Object[] parameter = null;
        //                Thread invokeFunctionThread = null;
        //                try {
        //                    if (targetMethodData.getArgTypes() != null) {
        //                        parameter = castValues(targetMethodData.getArgTypes(), params);
        //                    }
        //
        //                    Callable<VariantData> invokeMethod = invokeMethod(instance, targetMethodData, parameter);
        //                    FutureTask<VariantData> invokeFunctionTask = new FutureTask<>(invokeMethod);
        //                    invokeFunctionThread = new Thread(invokeFunctionTask);
        //
        //                    executorService.submit(invokeFunctionThread);
        //
        //                    returnVariantDataList[i] = invokeFunctionTask.get();
        //                    logger.info("is ok");
        //                    returnStatuses[i] = new APIResponse(SUCCESS_CODE, "success");
        //                } catch (Throwable e) {
        //                    returnVariantDataList[i] = null;
        //                    returnStatuses[i] = new APIResponse(ERROR_CODE, e.getMessage());
        //                    throw e;
        //                }
        //            }
        //            List<Variant> returnValues = null;
        //            if (returnType != void.class) {
        //                returnValues = new ArrayList<>(amountParamRows);
        //                for (VariantData returnVariantData : returnVariantDataList) {
        //                    if (returnVariantData == null) {
        //                        returnValues.add(null);
        //                    } else {
        //                        returnValues.add(mapVariantDataToVariant(returnVariantData));
        //                    }
        //                }
        //            }
        //
        //            return new ReturnValue(serialize(instance), returnValues, externalContractsStateByteCode,
        //                                   new ArrayList<>(asList(returnStatuses)));
        //
        //        } catch (Throwable e) {
        //            System.out.println("root cause message  - " + getRootCauseMessage(e));
        //            e.printStackTrace();
        //            throw new ContractExecutorException(
        //                "Cannot executeSmartContract the contract " + initiatorAddress + ". Reason: " + getRootCauseMessage(e));
        //        }
        return null;
    }

    private SmartContractMethodResult invokeSmartContractMethod(
        InvokeMethodSession session,
        Object instance,
        MethodData methodData) throws ContractExecutorException {

        Thread invokeFunctionThread = null;
        try {
            final Object[] parameter = methodData.argTypes != null ? castValues(methodData.argTypes, methodData.argValues) : null;
            final FutureTask<Variant> invokeMethodTask = new FutureTask<>(() -> mapObjectToVariant(methodData.method.invoke(instance, parameter)));
            invokeFunctionThread = new Thread(invokeMethodTask);
            initSessionSmartContractConstants(invokeFunctionThread.getId(), session.initiatorAddress, session.contractAddress, session.accessId);
            executorService.submit(invokeFunctionThread);

            return new SmartContractMethodResult(
                new APIResponse(SUCCESS_CODE, "success"),
                invokeMethodTask.get(session.executionTime, TimeUnit.MILLISECONDS));

        } catch (TimeoutException ex) {
            logger.info("timeout exception");
            invokeFunctionThread.stop();
            return new SmartContractMethodResult(new APIResponse(ERROR_CODE, "timeout exception"), null);

        } catch (Throwable e) {
            return new SmartContractMethodResult(new APIResponse(ERROR_CODE, e.getMessage()), null);
        }
    }

    private Permissions createServiceApiPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new ReflectPermission("suppressAccessChecks"));
        permissions.add(new NetPermission("getProxySelector"));
        permissions.add(new RuntimePermission("readFileDescriptor"));
        permissions.add(new RuntimePermission("writeFileDescriptor"));
        permissions.add(new RuntimePermission("accessDeclaredMembers"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.ec"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.rsa"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.provider"));
        permissions.add(new RuntimePermission("java.lang.RuntimePermission", "loadLibrary.sunec"));
        permissions.add(new RuntimePermission("java.lang.RuntimePermission", "createClassLoader"));
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
        permissions.add(new SocketPermission(properties.apiHost + ":" + properties.executorNodeApiPort, "connect,listen,resolve"));
        return permissions;
    }

    private Permissions createSmartContactPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new ReflectPermission("suppressAccessChecks"));
        permissions.add(new RuntimePermission("accessDeclaredMembers"));
        permissions.add(new RuntimePermission("java.lang.RuntimePermission", "createClassLoader"));
        return permissions;
    }

}

