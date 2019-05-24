package com.credits.service.contract;

import com.credits.ApplicationProperties;
import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.pojo.*;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.secure.PermissionsManager;
import com.credits.service.node.apiexec.NodeApiExecInteractionServiceImpl;
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
import java.util.*;
import java.util.concurrent.Executors;

import static com.credits.general.serialize.Serializer.deserialize;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.service.BackwardCompatibilityService.allVersionsSmartContractClass;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.utils.ContractExecutorServiceUtils.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


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

        permissionManager.grantAllPermissions(NodeApiExecInteractionServiceImpl.class);
        this.permissionManager = permissionManager;
    }

    @Override
    public ReturnValue deploySmartContract(DeployContractSession session) throws ContractExecutorException {
        final ByteCodeContractClassLoader byteCodeContractClassLoader = getSmartContractClassLoader();
        final Class<?> contractClass = compileClassAndDropPermissions(session.byteCodeObjectDataList, byteCodeContractClassLoader);

        return new Deployer(session, contractClass).deploy();
    }

    @Override
    public ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException {
        final ByteCodeContractClassLoader byteCodeContractClassLoader = getSmartContractClassLoader();
        final Class<?> contractClass = compileClassAndDropPermissions(session.byteCodeObjectDataList, byteCodeContractClassLoader);
        final Object instance = deserialize(session.contractState, byteCodeContractClassLoader);

        initializeSmartContractField("initiator", session.initiatorAddress, contractClass, instance);
        initializeSmartContractField("accessId", session.accessId, contractClass, instance);
        initializeSmartContractField("usedContracts", session.usedContracts, contractClass, instance);

        ExternalSmartContract usedContract = new ExternalSmartContract(
                new SmartContractGetResultData(
                        new ApiResponseData(ApiResponseCode.SUCCESS, ""),
                        session.byteCodeObjectDataList,
                        session.contractState,
                        true));
        usedContract.setInstance(instance);
        session.usedContracts.put(session.contractAddress, usedContract);

        return executeContractMethod(session, instance);
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
                methodAnnotationDataList.addAll(readAnnotation(annotation));
            }
            for (Parameter parameter : method.getParameters()) {
                List<AnnotationData> paramAnnotationDataList = new ArrayList<>();
                for (Annotation annotation : parameter.getAnnotations()) {
                    List<AnnotationData> parameterAnnotationData = readAnnotation(annotation);
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

        session.usedContracts.putAll(usedContracts);
        Object instance = usedContracts.get(session.contractAddress).getInstance();

        if (instance == null) {
            final Class<?> contractClass = compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader).stream()
                    .filter(clazz -> !clazz.getName().contains("$"))
                    .findAny()
                    .orElseThrow(() -> new ContractExecutorException("contract class not compiled"));
            instance = deserialize(session.contractState, classLoader);

            initializeField("initiator", session.initiatorAddress, contractClass, instance);
            initializeField("accessId", session.accessId, contractClass, instance);
            initializeField("usedContracts", session.usedContracts, contractClass, instance);
            usedContracts.get(session.contractAddress).setInstance(instance);
        }

        return executeContractMethod(session, instance);
    }

    private ReturnValue executeContractMethod(InvokeMethodSession session, Object contractInstance) {
        final var executor = new MethodExecutor(session, contractInstance);
        final var methodResults = executor.execute();
        return new ReturnValue(serialize(executor.getSmartContractObject()),
                               methodResults.stream()
                                       .map(mr -> mr.exception == null
                                               ? new SmartContractMethodResult(SUCCESS_API_RESPONSE, mr.returnValue, mr.spentCpuTime)
                                               : new SmartContractMethodResult(failureApiResponse(mr.exception), mr.returnValue, mr.spentCpuTime))
                                       .collect(toList()),
                               session.usedContracts);
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

}
