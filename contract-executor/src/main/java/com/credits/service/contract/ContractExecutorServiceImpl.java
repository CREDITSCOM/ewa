package com.credits.service.contract;

import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.secure.PermissionsManager;
import com.credits.service.node.apiexec.NodeApiExecInteractionServiceImpl;
import com.credits.thrift.utils.ContractExecutorUtils;
import exception.ContractExecutorException;
import pojo.ExternalSmartContract;
import pojo.ReturnValue;
import pojo.SmartContractMethodResult;
import pojo.apiexec.SmartContractGetResultData;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.credits.general.serialize.Serializer.deserialize;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.service.BackwardCompatibilityService.allVersionsSmartContractClass;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.thrift.utils.ContractExecutorUtils.getRootClass;
import static com.credits.utils.ContractExecutorServiceUtils.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final PermissionsManager permissionManager;

    @Inject
    public ContractExecutorServiceImpl(NodeApiExecInteractionService nodeApiExecService, PermissionsManager permissionManager) {
        this.permissionManager = permissionManager;
        allVersionsSmartContractClass.forEach(contract -> initStaticContractFields(nodeApiExecService, contract));
        permissionManager.grantAllPermissions(NodeApiExecInteractionServiceImpl.class);
    }

    @Override
    public ReturnValue deploySmartContract(DeployContractSession session) throws ContractExecutorException {
        final var contractClass = compileClassAndDropPermissions(session.byteCodeObjectDataList, getSmartContractClassLoader());
        return new Deployer(session, contractClass).deploy();
    }

    @Override
    public ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException {
        final var contractClassLoader = getSmartContractClassLoader();
        final var contractClass = compileClassAndDropPermissions(session.byteCodeObjectDataList, contractClassLoader);
        final var instance = deserialize(session.contractState, contractClassLoader);

        initNonStaticContractFields(session, contractClass, instance);
        addThisContractToUsedContracts(session, instance);
        return executeContractMethod(session, instance);
    }

    @Override
    public List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");

        final var contractClass = getRootClass(compileSmartContractByteCode(byteCodeObjectDataList, getSmartContractClassLoader()));
        return createMethodDescriptionListByClass(contractClass);
    }

    @Override
    public Map<String, Variant> getContractVariables(List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState)
    throws ContractExecutorException {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");
        requireNonNull(contractState, "contract state is null");
        if (contractState.length == 0) throw new ContractExecutorException("contract state is empty");

        var contractClassLoader = new ByteCodeContractClassLoader();
        compileSmartContractByteCode(byteCodeObjectDataList, contractClassLoader);
        return ContractExecutorUtils.getContractVariables(deserialize(contractState, contractClassLoader));
    }

    @Override
    public List<ByteCodeObjectData> compileClass(String sourceCode) throws ContractExecutorException, CompilationException {
        requireNonNull(sourceCode, "sourceCode of contract class is null");
        if (sourceCode.isEmpty()) throw new ContractExecutorException("sourceCode of contract class is empty");

        final var compilationPackage = InMemoryCompiler.compileSourceCode(sourceCode);
        return GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
    }

    @Override
    public ReturnValue executeExternalSmartContract(InvokeMethodSession session,
                                                    Map<String, ExternalSmartContract> usedContracts,
                                                    ByteCodeContractClassLoader classLoader) {
        requireNonNull(usedContracts, "usedContracts is null");

        session.usedContracts.putAll(usedContracts);
        var instance = usedContracts.get(session.contractAddress).getInstance();

        if (instance == null) {
            requireNonNull(classLoader, "classLoader is null");
            final var contractClass = getRootClass(compileSmartContractByteCode(session.byteCodeObjectDataList, classLoader));
            instance = deserialize(session.contractState, classLoader);
            initNonStaticContractFields(session, contractClass, instance);
            usedContracts.get(session.contractAddress).setInstance(instance);
        }

        return executeContractMethod(session, instance);
    }


    private void addThisContractToUsedContracts(InvokeMethodSession session, Object instance) {
        ExternalSmartContract usedContract = new ExternalSmartContract(
                new SmartContractGetResultData(
                        new ApiResponseData(ApiResponseCode.SUCCESS, ""),
                        session.byteCodeObjectDataList,
                        session.contractState,
                        true));
        usedContract.setInstance(instance);
        session.usedContracts.put(session.contractAddress, usedContract);
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

    private Class<?> compileClassAndDropPermissions(List<ByteCodeObjectData> byteCodeObjectList, ByteCodeContractClassLoader classLoader)
    throws ContractExecutorException {
        return compileSmartContractByteCode(byteCodeObjectList, classLoader).stream()
                .peek(permissionManager::dropSmartContractRights)
                .filter(clazz -> !clazz.getName().contains("$"))
                .findAny()
                .orElseThrow(() -> new CompilationException("contract class not compiled"));
    }

    private void initStaticContractFields(NodeApiExecInteractionService nodeApiExecService, Class<?> contract) {
        initializeSmartContractField("nodeApiService", nodeApiExecService, contract, null);
        initializeSmartContractField("contractExecutorService", this, contract, null);
        initializeSmartContractField("cachedPool", Executors.newCachedThreadPool(), contract, null);
    }

    private void initNonStaticContractFields(InvokeMethodSession session, Class<?> contractClass, Object instance) {
        initializeSmartContractField("initiator", session.initiatorAddress, contractClass, instance);
        initializeSmartContractField("accessId", session.accessId, contractClass, instance);
        initializeSmartContractField("usedContracts", session.usedContracts, contractClass, instance);
    }

}
