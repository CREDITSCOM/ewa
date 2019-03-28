package com.credits.service.contract;

import com.credits.ApplicationProperties;
import com.credits.classload.BytecodeContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.exception.CompilationException;
import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.pojo.ExternalSmartContract;
import com.credits.pojo.MethodData;
import com.credits.pojo.apiexec.SmartContractGetResultData;
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
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import java.util.logging.LoggingPermission;
import java.util.stream.Stream;

import static com.credits.general.serialize.Serializer.deserialize;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.general.util.variant.VariantConverter.toVariant;
import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.service.contract.SmartContractConstants.initSmartContractConstants;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static com.credits.utils.ContractExecutorServiceUtils.failureApiResponse;
import static com.credits.utils.ContractExecutorServiceUtils.getMethodArgumentsValuesByNameAndParams;
import static com.credits.utils.ContractExecutorServiceUtils.initializeField;
import static com.credits.utils.ContractExecutorServiceUtils.initializeSmartContractField;
import static com.credits.utils.ContractExecutorServiceUtils.parseAnnotationData;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
        } catch (Exception e) {
            logger.error("Cannot load smart contract's super class. Reason: ", e);
        }

        try {
            Class<?> serviceClass = Class.forName("com.credits.service.node.apiexec.NodeApiExecServiceImpl");
            Sandbox.confine(serviceClass, createServiceApiPermissions());
        } catch (Exception e) {
            logger.error("Cannot add permissions api service. Reason: ", e);
        }

        smartContractPermissions = createSmartContactPermissions();
    }

    @Override
    public ReturnValue deploySmartContract(DeployContractSession session) throws Exception {
        final BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
        final List<Class<?>> compiledClasses = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader);
        final Class<?> contractClass = compiledClasses.stream()
            .peek(clazz -> Sandbox.confine(clazz, smartContractPermissions))
            .filter(clazz -> !clazz.getName().contains("$"))
            .findAny()
            .orElseThrow(() -> new ClassNotFoundException("contract class not compiled"));

        return new ReturnValue(
            runForLimitTime(session, () -> serialize(contractClass.newInstance())),
            singletonList(new SmartContractMethodResult(SUCCESS_API_RESPONSE, null)), null);
    }

    @Override
    public ReturnValue executeSmartContract(InvokeMethodSession session) throws Exception {

        final BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
        final List<Class<?>> compiledClasses = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader);
        final Class<?> contractClass = compiledClasses.stream()
            .peek(clazz -> Sandbox.confine(clazz, smartContractPermissions))
            .filter(clazz -> !clazz.getName().contains("$"))
            .findAny()
            .orElseThrow(() -> new ClassNotFoundException("contract class not compiled"));

        final Object instance = deserialize(session.contractState, classLoader);

        final Map<String, ExternalSmartContract> usedSmartContracts = new HashMap<>();
        initializeField("initiator", session.initiatorAddress, contractClass, instance);
        initializeField("accessId", session.accessId, contractClass, instance);
        initializeField("usedContracts", usedSmartContracts, contractClass, instance);

        ExternalSmartContract usedContract = new ExternalSmartContract(
            new SmartContractGetResultData(
                new ApiResponseData(ApiResponseCode.SUCCESS, ""),
                session.byteCodeObjectDataList,
                session.contractState,
                true));
        usedContract.instance = instance;
        usedSmartContracts.put(session.contractAddress, usedContract);

        return session.paramsTable.length < 2
            ? invokeSingleMethod(session, instance, usedSmartContracts)
            : invokeMultipleMethod(session, instance, usedSmartContracts);
    }

    @Override
    public List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");

        BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
        Class<?> contractClass =
            compileSmartContractByteCode(byteCodeObjectDataList, classLoader).stream()
                .filter(clazz -> !clazz.getName().contains("$"))
                .findAny().get();

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
    public ReturnValue executeExternalSmartContract(InvokeMethodSession session, Map<String, ExternalSmartContract> usedContracts) {

        Object instance = usedContracts.get(session.contractAddress).instance;

        if (instance == null) {
            final BytecodeContractClassLoader classLoader = new BytecodeContractClassLoader();
            final Class<?> contractClass = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader).stream()
                .filter(clazz -> !clazz.getName().contains("$"))
                .findAny()
                .orElseThrow(() -> new ContractExecutorException("contract class not compiled"));
            instance = deserialize(session.contractState, classLoader);

            initializeField("initiator", session.initiatorAddress, contractClass, instance);
            initializeField("accessId", session.accessId, contractClass, instance);
            initializeField("usedContracts", usedContracts, contractClass, instance);
            usedContracts.get(session.contractAddress).instance = instance;
        }

        return invokeSingleMethod(session, instance, usedContracts);
    }

    private ReturnValue invokeSingleMethod(
        InvokeMethodSession session,
        Object instance,
        Map<String, ExternalSmartContract> usedContracts) {
        final SmartContractMethodResult result = invokeMethodAndCatchErrors(session, instance, session.paramsTable[0]);
        return new ReturnValue(serialize(instance), singletonList(result), usedContracts);
    }

    private ReturnValue invokeMultipleMethod(
        InvokeMethodSession session,
        Object instance,
        Map<String, ExternalSmartContract> usedContracts) {
        return stream(session.paramsTable)
            .flatMap(params -> Stream.of(invokeMethodAndCatchErrors(session, instance, params)))
            .reduce(
                new ReturnValue(null, new ArrayList<>(), usedContracts),
                (returnValue, result) -> {
                    returnValue.newContractState = returnValue.newContractState == null ? serialize(instance) : returnValue.newContractState;
                    returnValue.executeResults.add(result);
                    return returnValue;
                },
                (returnValue, returnValue2) -> returnValue);
    }

    private SmartContractMethodResult invokeMethodAndCatchErrors(InvokeMethodSession session, Object instance, Variant[] params) {
        try {
            final Object result = invoke(session, instance, params);
            return new SmartContractMethodResult(SUCCESS_API_RESPONSE, toVariant(result));
        } catch (Throwable e) {
            return new SmartContractMethodResult(failureApiResponse(e), null);
        }
    }

    private Object invoke(InvokeMethodSession session, Object instance, Variant[] params) throws Exception {
        MethodData methodData = getMethodArgumentsValuesByNameAndParams(instance.getClass(), session.methodName, params);
        return runForLimitTime(session, () -> methodData.method.invoke(instance, methodData.argValues));
    }

    private <R> R runForLimitTime(DeployContractSession session, Callable<R> block) throws Exception {
        Thread limitedTimeThread = null;
        try {
            FutureTask<R> task = new FutureTask<>(block);
            limitedTimeThread = new Thread(task);
            initSmartContractConstants(limitedTimeThread.getId(), session);
            //            executorService.submit(limitedTimeThread);
            limitedTimeThread.start();
            return task.get(session.executionTime, MILLISECONDS);
        } catch (TimeoutException e) {
            // TODO: 3/19/2019 create thread wrapper for correct stop thread
            limitedTimeThread.stop();
            throw new TimeoutException(e.getMessage());
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
        permissions.add(new LoggingPermission("control", null));
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

