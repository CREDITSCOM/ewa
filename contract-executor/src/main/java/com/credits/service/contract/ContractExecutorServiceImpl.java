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
import com.credits.secure.Sandbox;
import com.credits.service.contract.session.DeployContractSession;
import com.credits.service.contract.session.InvokeMethodSession;
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
import static com.credits.service.contract.SmartContractConstants.initSmartContractConstants;
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
            initializeSmartContractField("nodeApiService", dbInteractionService, contract, null);
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
    public ReturnValue deploySmartContract(DeployContractSession session) {
        try {
            BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
            Class<?> contractClass = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader);

            Sandbox.confine(contractClass, smartContractPermissions);

            initSmartContractConstants(currentThread().getId(), session);

            return new ReturnValue(
                serialize(contractClass.newInstance()),
                singletonList(new SmartContractMethodResult(new APIResponse(SUCCESS_CODE, "success"), null)), null);

        } catch (Throwable e) {
            logger.debug("Cannot deploy the contract. Root cause message: {}\n{}", getRootCauseMessage(e), e);
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

            final Map<String, ByteBuffer> externalContractStates = new HashMap<>();
            initializeField("initiator", session.initiatorAddress, contractClass, instance);
            initializeField("accessId", session.accessId, contractClass, instance);
            initializeField("externalContractsStates", externalContractStates, contractClass, instance);

            //todo add "void" type to Variant
            return invokeMultipleMethod(session, contractClass, instance, externalContractStates);

        } catch (Throwable e) {
            logger.debug(
                "Cannot execute SmartContract the contract. Root cause message: {}\n",
                getRootCauseMessage(e),
                e);
            throw new ContractExecutorException(
                "Cannot execute the SmartContract " + session.contractAddress + ", accessId " + session.accessId +
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
                List<AnnotationData> methodAnnotationData = parseAnnotationData(annotation.toString());
                methodAnnotationDataList.addAll(methodAnnotationData);
            }
            for (Parameter parameter : method.getParameters()) {
                List<AnnotationData> paramAnnotationDataList = new ArrayList<>();
                for (Annotation annotation : parameter.getAnnotations()) {
                    List<AnnotationData> parameterAnnotationData = parseAnnotationData(annotation.toString());
                    paramAnnotationDataList.addAll(parameterAnnotationData);
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
    public ReturnValue executeExternalSmartContract(InvokeMethodSession session, Map<String, ByteBuffer> contractsStates) {

        BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
        Class<?> contractClass = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader);
        Object instance = deserialize(session.contractState, classLoader);

        initializeField("initiator", session.initiatorAddress, contractClass, instance);
        initializeField("accessId", session.accessId, contractClass, instance);
        initializeField("externalContractsStates", contractsStates, contractClass, instance);

        return invokeMultipleMethod(session, contractClass, instance, contractsStates);

    }

    private ReturnValue invokeMultipleMethod(
        InvokeMethodSession session,
        Class<?> contractClass,
        Object instance,
        Map<String, ByteBuffer> externalContractStates) {
        return stream(session.paramsTable)
            .flatMap(params -> {
                MethodData methodData = getMethodArgumentsValuesByNameAndParams(contractClass, session.methodName, params);
                return Stream.of(invokeMethod(session, instance, methodData));
            })
            .reduce(
                new ReturnValue(null, new ArrayList<>(), externalContractStates),
                (returnValue, smartContractMethodResult) -> {
                    if (returnValue.newContractState == null) {
                        returnValue.newContractState = serialize(instance);
                    }
                    System.out.println(smartContractMethodResult);
                    returnValue.executeResults.add(smartContractMethodResult);
                    return returnValue;
                },
                (returnValue, returnValue2) -> returnValue);
    }

    private SmartContractMethodResult invokeMethod(
        InvokeMethodSession session,
        Object instance,
        MethodData methodData) throws ContractExecutorException {

        Thread invokeFunctionThread = null;
        try {
            final Object[] parameter = methodData.argTypes != null ? castValues(methodData.argTypes, methodData.argValues) : null;
            final FutureTask<SmartContractMethodResult> invokeMethodTask = new FutureTask<>(() -> {
                try {
                    return new SmartContractMethodResult(
                        new APIResponse(SUCCESS_CODE, "success"),
                        mapObjectToVariant(methodData.method.invoke(instance, parameter)));
                } catch (Throwable e) {
                    return new SmartContractMethodResult(new APIResponse(ERROR_CODE, getRootCauseMessage(e)), null);
                }
            });
            invokeFunctionThread = new Thread(invokeMethodTask);
            initSmartContractConstants(invokeFunctionThread.getId(), session);
            executorService.submit(invokeFunctionThread);

            return invokeMethodTask.get(session.executionTime, TimeUnit.MILLISECONDS);

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

