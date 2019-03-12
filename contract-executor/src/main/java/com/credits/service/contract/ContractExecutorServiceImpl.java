package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.exception.CompilationException;
import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.variant.VariantConverter;
import com.credits.general.util.variant.VariantUtils;
import com.credits.pojo.MethodArgumentsValuesData;
import com.credits.secure.PermissionManager;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractExecutorUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.serialize.Serializer.deserialize;
import static com.credits.serialize.Serializer.serialize;
import static com.credits.service.contract.SmartContractConstants.initSessionSmartContractConstants;
import static com.credits.thrift.ContractExecutorHandler.ERROR_CODE;
import static com.credits.thrift.ContractExecutorHandler.SUCCESS_CODE;
import static com.credits.utils.ContractExecutorServiceUtils.castValues;
import static com.credits.utils.ContractExecutorServiceUtils.getArgTypes;
import static com.credits.utils.ContractExecutorServiceUtils.initializeField;
import static com.credits.utils.ContractExecutorServiceUtils.initializeSmartContractField;
import static com.credits.utils.ContractExecutorServiceUtils.parseAnnotationData;
import static com.credits.utils.ContractExecutorServiceUtils.writeLog;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);
    private ExecutorService executorService;
    private PermissionManager permissionManager;

    public ContractExecutorServiceImpl(NodeApiExecInteractionService dbInteractionService) {
        executorService = Executors.newCachedThreadPool();
        INJECTOR.component.inject(this);
        try {
            permissionManager = new PermissionManager();
            permissionManager.createPermissionsForByteArrayClassLoaderConstructor();
            permissionManager.createPermissionsForNodeApiExecService();

            Class<?> contract = Class.forName("SmartContract");
            initializeSmartContractField("service", dbInteractionService, contract, null);
            initializeSmartContractField("contractExecutorService", this, contract, null);
            initializeSmartContractField("cachedPool", Executors.newCachedThreadPool(), contract, null);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load smart contract's super class", e);
        }
    }

    @Override
    public ReturnValue execute(long accessId, byte[] initiatorAddress, byte[] contractAddress,
        List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState, String methodName,
        Variant[][] paramsTable, long executionTime) throws ContractExecutorException {
        String initiatorAddressBase58 = "unknown address";
        String contractAddressBase58;
        try {
            if (byteCodeObjectDataList.size() == 0) {
                throw new ContractExecutorException("Bytecode size is 0");
            }
            requireNonNull(initiatorAddress, "initiatorAddress is null");
            requireNonNull(contractAddress, "contractAddress is null");
            initiatorAddressBase58 = Base58.encode(initiatorAddress);
            contractAddressBase58 = Base58.encode(contractAddress);

            ByteArrayContractClassLoader classLoader =
                new ByteArrayClassLoaderConstructor().getByteArrayContractClassLoader();

            Class<?> contractClass =
                ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);

            permissionManager.createPermissionsForSmartContractClass(contractClass);

            if (contractState != null && contractState.length != 0) {

                Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode = new HashMap<>();

                Object instance =
                    deserializeInstance(accessId, initiatorAddressBase58, contractAddressBase58, contractState,
                        classLoader, contractClass, externalContractsStateByteCode);

                return executeSmartContract(methodName, paramsTable, contractAddressBase58, contractClass, instance,
                    externalContractsStateByteCode, executionTime);
            } else {
                return createNewSmartContractInstance(accessId, initiatorAddressBase58, contractAddressBase58,
                    contractClass);
            }
        } catch (Throwable e) {
            System.out.println("root cause message  - " + getRootCauseMessage(e));
            e.printStackTrace();
            throw new ContractExecutorException(
                "Cannot execute the contract " + initiatorAddressBase58 + ". Reason: " + getRootCauseMessage(e));
        }
    }

    private ReturnValue createNewSmartContractInstance(long accessId, String initiatorAddressBase58,
        String contractAddressBase58, Class<?> contractClass) throws InstantiationException, IllegalAccessException {
        writeLog("Start create new Instance of smart contract " + contractAddressBase58);
        Object instance;
        initSessionSmartContractConstants(Thread.currentThread().getId(), initiatorAddressBase58, contractAddressBase58,
            accessId);
        instance = contractClass.newInstance();
        ReturnValue returnValue = new ReturnValue(serialize(instance), null, null,
            Collections.singletonList(new APIResponse(SUCCESS_CODE, "success")));
        writeLog("End create new Instance of smart contract " + contractAddressBase58);
        return returnValue;
    }


    static MethodArgumentsValuesData getMethodArgumentsValuesByNameAndParams(Class<?> contractClass, String methodName,
        Variant[] params) {
        if (params == null) {
            throw new ContractExecutorException("Cannot find method params == null");
        }

        Class[] argTypes = getArgTypes(params);
        Method method = MethodUtils.getMatchingAccessibleMethod(contractClass, methodName, argTypes);
        if (method != null) {
            return new MethodArgumentsValuesData(method, argTypes, params);
        } else {
            throw new ContractExecutorException("Cannot find a method by name and parameters specified");
        }
    }

    private Callable<VariantData> invokeFunction(Object instance, MethodArgumentsValuesData targetMethodData,
        Object[] parameter) {
        return () -> VariantConverter.objectToVariantData(targetMethodData.getMethod().invoke(instance, parameter));
    }


    @Override
    public List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");
        ByteArrayContractClassLoader classLoader =
            new ByteArrayClassLoaderConstructor().getByteArrayContractClassLoader();

        Class<?> contractClass =
            ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
        Set<String> objectMethods = new HashSet<>(
            Arrays.asList("getClass", "hashCode", "equals", "toString", "notify", "notifyAll", "wait", "finalize"));
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
    public Map<String, Variant> getContractVariables(List<ByteCodeObjectData> byteCodeObjectDataList,
        byte[] contractState) throws ContractExecutorException {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");
        requireNonNull(contractState, "contract state is null");

        if (contractState.length != 0) {
            ByteArrayContractClassLoader classLoader =
                new ByteArrayClassLoaderConstructor().getByteArrayContractClassLoader();


            Class<?> contractClass =
                ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
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
    public ReturnValue executeExternalSmartContract(long accessId, String initiatorAddress,
        String externalSmartContractAddress, String externalSmartContractMethod,
        List<Object> externalSmartContractParams, List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState,
        Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode) throws ExecutionException, InterruptedException {


        if (byteCodeObjectDataList.size() == 0) {
            throw new ContractExecutorException("Bytecode size is 0");
        }
        logger.info("Start execute method {} external smart contract {} from initiator {}",
            externalSmartContractAddress, initiatorAddress, externalSmartContractMethod);

        ByteArrayContractClassLoader classLoader =
            new ByteArrayClassLoaderConstructor().getByteArrayContractClassLoader();

        Class<?> contractClass =
            ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);


        Object instance =
            deserializeInstance(accessId, initiatorAddress, externalSmartContractAddress, contractState, classLoader,
                contractClass, externalContractsStateByteCode);

        requireNonNull(initiatorAddress, "initiatorAddress is null");
        requireNonNull(externalSmartContractAddress, "contractAddress is null");

        Variant[] params = new Variant[externalSmartContractParams.size()];
        for (int i = 0; i < externalSmartContractParams.size(); i++) {
            Variant variant = ContractExecutorUtils.mapObjectToVariant(externalSmartContractParams.get(i));
            params[i] = variant;
        }

        Variant[][] variantsParams = new Variant[][] {{}};
        variantsParams[0] = params;


        ReturnValue returnValue =
            executeSmartContract(externalSmartContractMethod, variantsParams, externalSmartContractAddress,
                contractClass, instance, externalContractsStateByteCode, null);

        logger.info("Stop execute method {} external smart contract {} from initiator {}", externalSmartContractAddress,
            initiatorAddress, externalSmartContractMethod);
        return returnValue;
    }

    private ReturnValue executeSmartContract(String methodName, Variant[][] paramsTable, String contractAddress,
        Class<?> contractClass, Object instance, Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode,
        Long executionTime) throws InterruptedException, java.util.concurrent.ExecutionException {
        writeLog("Start execute method " + methodName + " of smart contract" + contractAddress);
        int amountParamRows = 1;
        Variant[] params = null;
        if (paramsTable != null && paramsTable.length > 0) {
            amountParamRows = paramsTable.length;
            params = paramsTable[0];
        }

        MethodArgumentsValuesData targetMethodData =
            getMethodArgumentsValuesByNameAndParams(contractClass, methodName, params);

        VariantData[] returnVariantDataList = new VariantData[amountParamRows];
        APIResponse[] returnStatuses = new APIResponse[amountParamRows];

        Class<?> returnType = targetMethodData.getMethod().getReturnType();

        for (int i = 0; i < amountParamRows; i++) {
            Object[] parameter = null;
            Thread invokeFunctionThread = null;
            try {
                if (targetMethodData.getArgTypes() != null) {
                    if (paramsTable[i] != null) {
                        parameter = castValues(targetMethodData.getArgTypes(), paramsTable[i]);
                    }
                }
                Callable<VariantData> invokeFunction = invokeFunction(instance, targetMethodData, parameter);
                FutureTask<VariantData> invokeFunctionTask = new FutureTask<>(invokeFunction);
                invokeFunctionThread = new Thread(invokeFunctionTask);
                executorService.submit(invokeFunctionThread);
                if (executionTime != null) {
                    returnVariantDataList[i] = invokeFunctionTask.get(executionTime, TimeUnit.MILLISECONDS);
                } else {
                    returnVariantDataList[i] = invokeFunctionTask.get();
                }
                writeLog("Execute method " + methodName + " of smart contract " + contractAddress + " was successful");
                returnStatuses[i] = new APIResponse(SUCCESS_CODE, "success");
            } catch (TimeoutException ex) {
                returnVariantDataList[i] = null;
                String message = "Execute method " + methodName + " of smart contract " + contractAddress +
                    " was stopped with timeout exception";
                writeLog(message);
                invokeFunctionThread.stop();
                returnStatuses[i] = new APIResponse(ERROR_CODE, "Smart contract execution has timeout exception");
                if (amountParamRows == 1) {
                    throw new ContractExecutorException(message);
                }
            } catch (Throwable e) {
                writeLog("Execute method " + methodName + " of smart contract " + contractAddress +
                    " was stopped with exception" + e.getMessage());
                returnVariantDataList[i] = null;
                returnStatuses[i] = new APIResponse(ERROR_CODE, e.getMessage());
                if (amountParamRows == 1) {
                    throw e;
                }
            }
        }
        List<Variant> returnValues = createReturnVariantList(returnVariantDataList, returnType);

        return new ReturnValue(serialize(instance), returnValues, externalContractsStateByteCode,
            new ArrayList<>(Arrays.asList(returnStatuses)));
    }

    private Object deserializeInstance(long accessId, String initiatorAddressBase58, String contractAddressBase58,
        byte[] contractState, ByteArrayContractClassLoader classLoader, Class<?> contractClass,
        Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode) {
        writeLog("Start deserialize smart contract with address " + contractAddressBase58);
        Object instance;
        instance = deserialize(contractState, classLoader);
        initializeField("initiator", initiatorAddressBase58, contractClass, instance);
        initializeField("externalContracts", externalContractsStateByteCode, contractClass, instance);
        initializeField("contractAddress", contractAddressBase58, contractClass, instance);
        initializeField("accessId", accessId, contractClass, instance);
        writeLog("End deserialize smart contract with address " + contractAddressBase58);
        return instance;
    }

    private List<Variant> createReturnVariantList(VariantData[] returnVariantDataList, Class<?> returnType) {
        List<Variant> returnValues = new ArrayList<>();
        if (returnType != void.class) {
            for (VariantData returnVariantData : returnVariantDataList) {
                if (returnVariantData == null) {
                    returnValues.add(new Variant(Variant._Fields.V_NULL, VariantUtils.NULL_TYPE_VALUE));
                } else {
                    returnValues.add(ContractExecutorUtils.mapVariantDataToVariant(returnVariantData));
                }
            }
        } else {
            returnValues.add(new Variant(Variant._Fields.V_NULL, VariantUtils.NULL_TYPE_VALUE));
        }
        return returnValues;
    }
}