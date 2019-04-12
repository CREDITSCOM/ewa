package com.credits.service.contract;

import com.credits.ApplicationProperties;
import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.pojo.MethodData;
import com.credits.secure.PermissionsManager;
import com.credits.service.node.apiexec.NodeApiExecServiceImpl;
import com.credits.thrift.utils.ContractExecutorUtils;
import exception.ContractExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.ExternalSmartContract;
import pojo.ReturnValue;
import pojo.SmartContractMethodResult;
import pojo.apiexec.SmartContractGetResultData;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.credits.general.serialize.Serializer.deserialize;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.general.util.variant.VariantConverter.toVariant;
import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.service.BackwardCompatibilityService.allVersionsBasicStandardClass;
import static com.credits.service.BackwardCompatibilityService.allVersionsSmartContractClass;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.utils.Constants.CREDITS_TOKEN_NAME;
import static com.credits.utils.Constants.CREDITS_TOKEN_SYMBOL;
import static com.credits.utils.Constants.TOKEN_NAME_RESERVED_ERROR;
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
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;
import static pojo.SmartContractConstants.initSmartContractConstants;


public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);

    private final PermissionsManager permissionManager;

    @Inject
    public ApplicationProperties properties;

    public ContractExecutorServiceImpl(NodeApiExecInteractionService nodeApiExecService, PermissionsManager permissionManager) {
        INJECTOR.component.inject(this);
        try {
            allVersionsSmartContractClass.forEach(contract -> {
                initializeSmartContractField("nodeApiService", nodeApiExecService, contract, null);
                initializeSmartContractField("contractExecutorService", this, contract, null);
                initializeSmartContractField("cachedPool", Executors.newCachedThreadPool(), contract, null);
            });
        } catch (Exception e) {
            logger.error("Cannot load smart contract's super class. Reason: ", e);
        }

        permissionManager.grantAllPermissions(NodeApiExecServiceImpl.class);
        this.permissionManager = permissionManager;
    }

    @Override
    public ReturnValue deploySmartContract(DeployContractSession session) throws ContractExecutorException {
        final ByteCodeContractClassLoader byteCodeContractClassLoader = getSmartContractClassLoader();
        final Class<?> contractClass = compileClassAndDropPermissions(session.byteCodeObjectDataList, byteCodeContractClassLoader);
        final Object instance = runForLimitTime(session, byteCodeContractClassLoader, contractClass::newInstance);

        checkThatIsNotCreditsToken(contractClass, instance);
        return new ReturnValue(serialize(instance), singletonList(new SmartContractMethodResult(SUCCESS_API_RESPONSE, null)), null);
    }

    @Override
    public ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException {
        final ByteCodeContractClassLoader byteCodeContractClassLoader = getSmartContractClassLoader();
        final Class<?> contractClass = compileClassAndDropPermissions(session.byteCodeObjectDataList, byteCodeContractClassLoader);
        final Object instance = deserialize(session.contractState, byteCodeContractClassLoader);

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
            ? invokeSingleMethod(session, instance, byteCodeContractClassLoader, usedSmartContracts)
            : invokeMultipleMethod(session, instance, byteCodeContractClassLoader, usedSmartContracts);
    }

    @Override
    public List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");

        ByteCodeContractClassLoader byteCodeContractClassLoader = getSmartContractClassLoader();
        Class<?> contractClass = compileSmartContractByteCode(byteCodeObjectDataList, byteCodeContractClassLoader).stream()
            .filter(clazz -> !clazz.getName().contains("$"))
            .findAny()
            .orElseThrow(() -> new ContractExecutorException("contract class not compiled"));

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
            ByteCodeContractClassLoader byteCodeContractClassLoader = new ByteCodeContractClassLoader();
            compileSmartContractByteCode(byteCodeObjectDataList, byteCodeContractClassLoader);
            return ContractExecutorUtils.getContractVariables(deserialize(contractState, byteCodeContractClassLoader));
        } else {
            throw new ContractExecutorException("contract state is empty");
        }
    }

    @Override
    public List<ByteCodeObjectData> compileClass(String sourceCode) throws ContractExecutorException, CompilationException {
        requireNonNull(sourceCode, "sourceCode of contract class is null");
        if (sourceCode.isEmpty()) {
            throw new ContractExecutorException("sourceCode of contract class is empty");
        }
        CompilationPackage compilationPackage = InMemoryCompiler.compileSourceCode(sourceCode);
        return GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
    }

    @Override
    public ReturnValue executeExternalSmartContract(
        InvokeMethodSession session,
        Map<String, ExternalSmartContract> usedContracts,
        ByteCodeContractClassLoader classLoader) {

        Object instance = usedContracts.get(session.contractAddress).instance;

        if (instance == null) {
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

        return invokeSingleMethod(session, instance, classLoader, usedContracts);
    }

    private ReturnValue invokeSingleMethod(
        InvokeMethodSession session,
        Object instance,
        ByteCodeContractClassLoader byteCodeContractClassLoader, Map<String, ExternalSmartContract> usedContracts) {

        final SmartContractMethodResult result = invokeMethodAndCatchErrors(session, instance, session.paramsTable[0], byteCodeContractClassLoader);
        return new ReturnValue(serialize(instance), singletonList(result), usedContracts);
    }

    private ReturnValue invokeMultipleMethod(
        InvokeMethodSession session,
        Object instance,
        ByteCodeContractClassLoader byteCodeContractClassLoader,
        Map<String, ExternalSmartContract> usedContracts) {
        return stream(session.paramsTable)
            .flatMap(params -> Stream.of(invokeMethodAndCatchErrors(session, instance, params, byteCodeContractClassLoader)))
            .reduce(
                new ReturnValue(null, new ArrayList<>(), usedContracts),
                (returnValue, result) -> {
                    returnValue.newContractState = returnValue.newContractState == null ? serialize(instance) : returnValue.newContractState;
                    returnValue.executeResults.add(result);
                    return returnValue;
                },
                (returnValue, returnValue2) -> returnValue);
    }

    private SmartContractMethodResult invokeMethodAndCatchErrors(
        InvokeMethodSession session,
        Object instance,
        Variant[] params,
        ByteCodeContractClassLoader byteCodeContractClassLoader) {
        try {
            return new SmartContractMethodResult(SUCCESS_API_RESPONSE, invoke(session, instance, params, byteCodeContractClassLoader));
        } catch (Throwable e) {
            logger.debug("execution error:\ncontract address {},\nmethod {}\n", session.contractAddress, session.methodName, e);
            return new SmartContractMethodResult(failureApiResponse(e), new Variant(V_STRING, e.getMessage()));
        }
    }

    private Variant invoke(InvokeMethodSession session, Object instance, Variant[] params, ByteCodeContractClassLoader classLoader)
        throws Exception {

        final MethodData methodData = getMethodArgumentsValuesByNameAndParams(instance.getClass(), session.methodName, params, classLoader);
        final Method method = methodData.method;
        final String returnTypeName = method.getReturnType().getTypeName();
        return toVariant(returnTypeName, runForLimitTime(session, classLoader, () -> method.invoke(instance, methodData.argValues)));
    }

    private <R> R runForLimitTime(DeployContractSession session, ClassLoader classLoader, Callable<R> block) {
        Thread limitedTimeThread = null;
        try {
            FutureTask<R> task = new FutureTask<>(block);
            limitedTimeThread = new Thread(task);
            limitedTimeThread.setContextClassLoader(classLoader);
            initSmartContractConstants(limitedTimeThread.getId(), session);
            limitedTimeThread.start();
            return task.get(session.executionTime, MILLISECONDS);
        } catch (TimeoutException e) {
            // TODO: 3/19/2019 create thread wrapper for correct stop thread
            limitedTimeThread.stop();
            throw new ContractExecutorException(e.getMessage(), e);
        } catch (InterruptedException | ExecutionException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }


    private Class<?> compileClassAndDropPermissions(
        List<ByteCodeObjectData> byteCodeObjectList,
        ByteCodeContractClassLoader byteCodeContractClassLoader)
        throws ContractExecutorException {
        return compileSmartContractByteCode(byteCodeObjectList, byteCodeContractClassLoader).stream()
            .peek(permissionManager::dropSmartContractRights)
            .filter(clazz -> !clazz.getName().contains("$"))
            .findAny()
            .orElseThrow(() -> new CompilationException("contract class not compiled"));
    }

    private void checkThatIsNotCreditsToken(Class<?> contractClass, Object instance) {
        stream(contractClass.getInterfaces())
            .filter(allVersionsBasicStandardClass::contains)
            .findAny()
            .ifPresent(ignore -> stream(contractClass.getMethods())
                .filter(m -> m.getName().equals("getName") || m.getName().equals("getSymbol") && m.getParameters().length == 0)

                .forEach(method -> {
                    try {
                        String methodName = method.getName();
                        if (methodName.equals("getName")) {
                            if (((String) method.invoke(instance)).equalsIgnoreCase(CREDITS_TOKEN_NAME)) {
                                throw new ContractExecutorException(TOKEN_NAME_RESERVED_ERROR);
                            }
                        } else if (methodName.equals("getSymbol")) {
                            if (((String) method.invoke(instance)).equalsIgnoreCase(CREDITS_TOKEN_SYMBOL)) {
                                throw new ContractExecutorException(TOKEN_NAME_RESERVED_ERROR);
                            }
                        }
                    } catch (ContractExecutorException e) {
                        rethrow(e);
                    } catch (Throwable ignored) {
                    }
                }));
    }
}

