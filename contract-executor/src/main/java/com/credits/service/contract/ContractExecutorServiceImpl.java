package com.credits.service.contract;

import com.credits.ApplicationProperties;
import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.exception.CompilationException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.MethodArgument;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.variant.VariantConverter;
import com.credits.pojo.MethodArgumentsValuesData;
import com.credits.secure.Sandbox;
import com.credits.service.node.api.NodeApiInteractionService;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractExecutorUtils;
import com.credits.utils.ContractExecutorServiceUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.serialize.Serializer.deserialize;
import static com.credits.serialize.Serializer.serialize;
import static com.credits.thrift.ContractExecutorHandler.ERROR_CODE;
import static com.credits.thrift.ContractExecutorHandler.SUCCESS_CODE;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);
    private ExecutorService executorService;

    @Inject
    public ApplicationProperties properties;


    public ContractExecutorServiceImpl(NodeApiInteractionService dbInteractionService) {
        executorService = Executors.newCachedThreadPool();
        INJECTOR.component.inject(this);
        try {
            Class<?> contract = Class.forName("SmartContract");
            Field interactionService = contract.getDeclaredField("service");
            interactionService.setAccessible(true);
            interactionService.set(null, dbInteractionService);

            Field cachedPoolField = contract.getDeclaredField("cachedPool");
            cachedPoolField.setAccessible(true);
            ExecutorService cachedPool = Executors.newCachedThreadPool();
            cachedPoolField.set(null, cachedPool);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot load smart contract's super class", e);
        }
    }


    @Override
    public ReturnValue execute(long accessId, byte[] initiatorAddress, byte[] contractAddress, List<ByteCodeObjectData> byteCodeObjectDataList,  byte[] contractState,  String methodName,
         Variant[][] paramsTable, long executionTime) throws ContractExecutorException {

        String initiatorAddressBase58 = "unknown address";
        String contractAddressBase58 = "unknown address";
        try {
            if(byteCodeObjectDataList.size()==0) {
                throw new ContractExecutorException("Bytecode size is 0");
            }
            requireNonNull(initiatorAddress, "initiatorAddress is null");
            requireNonNull(contractAddress, "contractAddress is null");
            initiatorAddressBase58 = Base58.encode(initiatorAddress);
            contractAddressBase58 = Base58.encode(contractAddress);


            ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();
            Class<?> contractClass = ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);

            // add classes to Sandbox
            Sandbox.confine(contractClass, createPermissions());
            Class<?> serviceClass;
            try {
                serviceClass = Class.forName("com.credits.service.node.api.NodeApiInteractionServiceThriftImpl");
            } catch (ClassNotFoundException e) {
                throw new ContractExecutorException("", e);
            }
            Permissions permissions = createPermissions();
            permissions.add(new SocketPermission(properties.apiHost + ":" + properties.apiPort, "connect,listen,resolve"));
            Sandbox.confine(serviceClass, permissions);

            Object instance;
            if (contractState != null && contractState.length != 0) {
                instance = deserialize(contractState, classLoader);
                ContractExecutorServiceUtils.initializeField("initiator", initiatorAddressBase58, contractClass, instance);
            } else {
                instance = contractClass.getDeclaredConstructor(String.class).newInstance(initiatorAddressBase58);
                return new ReturnValue(serialize(instance), null, Collections.singletonList(new APIResponse(SUCCESS_CODE, "success")));
            }

            ContractExecutorServiceUtils.initializeField("contractAddress", contractAddressBase58, contractClass, instance);
            ContractExecutorServiceUtils.initializeField("accessId", accessId, contractClass, instance);

            int amountParamRows = 1;
            Variant[] params = null;
            if (paramsTable != null && paramsTable.length > 0) {
                amountParamRows = paramsTable.length;
                params = paramsTable[0];
            }

            MethodArgumentsValuesData targetMethodData = getMethodArgumentsValuesByNameAndParams(contractClass, methodName, params);

            VariantData[] returnVariantDataList = new VariantData[amountParamRows];
            APIResponse[] returnStatuses = new APIResponse[amountParamRows];

            Class<?> returnType = targetMethodData.getMethod().getReturnType();

            for (int i = 0; i < amountParamRows; i++) {
                Object[] parameter = null;
                Thread invokeFunctionThread = null;
                try {
                    if (targetMethodData.getArgTypes() != null) {
                        if (paramsTable != null && paramsTable[i] != null) {
                            parameter = ContractExecutorServiceUtils.castValues(targetMethodData.getArgTypes(), paramsTable[i]);
                        }
                    }

                    Callable<VariantData> invokeFunction = invokeFunction(instance, targetMethodData, parameter);
                    FutureTask<VariantData> invokeFunctionTask = new FutureTask<>(invokeFunction);
                    invokeFunctionThread = new Thread(invokeFunctionTask);

                    executorService.submit(invokeFunctionThread);

                    returnVariantDataList[i] = invokeFunctionTask.get(executionTime, TimeUnit.MILLISECONDS); // will wait for the async completion
                    logger.info("is ok");
                    returnStatuses[i] = new APIResponse(SUCCESS_CODE, "success");
                } catch (TimeoutException ex) {
                    returnVariantDataList[i] = null;
                    logger.info("timeout exception");
                    invokeFunctionThread.stop();
                    returnStatuses[i] = new APIResponse(ERROR_CODE, "timeout exception");
                    if (amountParamRows == 1) {
                        throw new ContractExecutorException("timeout exception. " + executorService);
                    }
                } catch (Throwable e) {
                    returnVariantDataList[i] = null;
                    returnStatuses[i] = new APIResponse(ERROR_CODE, e.getMessage());
                    if (amountParamRows == 1) {
                        throw e;
                    }
                }
            }
            List<Variant> returnValues = null;
            if (returnType != void.class) {
                returnValues = new ArrayList<>(amountParamRows);
                for (VariantData returnVariantData : returnVariantDataList) {
                    if (returnVariantData == null) {
                        returnValues.add(null);
                    } else {
                        returnValues.add(ContractExecutorUtils.mapVariantDataToVariant(returnVariantData));
                    }
                }
            }

            return new ReturnValue(serialize(instance), returnValues, new ArrayList<>(Arrays.asList(returnStatuses)));

        } catch (Throwable e) {
            System.out.println("root cause message  - " + getRootCauseMessage(e));
            e.printStackTrace();
            throw new ContractExecutorException(
                "Cannot execute the contract " + initiatorAddressBase58 + ". Reason: " + getRootCauseMessage(e));
        }
    }

    static MethodArgumentsValuesData getMethodArgumentsValuesByNameAndParams(Class<?> contractClass,
        String methodName, Variant[] params) {
        if (params==null) {
            throw new ContractExecutorException("Cannot find method params == null");
        }

        Class[] argTypes = ContractExecutorServiceUtils.getArgTypes(params);
        Method method = MethodUtils.getMatchingAccessibleMethod(contractClass, methodName, argTypes);
        if (method!=null) {
            return  new MethodArgumentsValuesData(method, argTypes, params);
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

        ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();
        Class<?> contractClass = ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
        Set<String> objectMethods = new HashSet<>(Arrays.asList("getClass", "hashCode", "equals", "toString", "notify", "notifyAll", "wait", "finalize"));
        List<MethodDescriptionData> result = new ArrayList<>();
        for (Method method : contractClass.getMethods()) {
            if (objectMethods.contains(method.getName())) continue;
            ArrayList<MethodArgument> args = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                args.add(new MethodArgument(parameter.getType().getTypeName(), parameter.getName()));
            }
            result.add(new MethodDescriptionData(method.getGenericReturnType().getTypeName(), method.getName(), args));
        }

        return result;
    }

    @Override
    public Map<String, Variant> getContractVariables(List<ByteCodeObjectData> byteCodeObjectDataList,  byte[] contractState) throws ContractExecutorException {
        requireNonNull(byteCodeObjectDataList, "bytecode of contract class is null");
        requireNonNull(contractState, "contract state is null");

        if (contractState.length != 0) {
            ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();
            Class<?> contractClass = ContractExecutorUtils.compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
            return ContractExecutorUtils.getContractVariables(deserialize(contractState, classLoader));
        } else {
            throw new ContractExecutorException("contract state is empty");
        }
    }

    @Override
    public List<ByteCodeObjectData> compileClass( String sourceCode) throws ContractExecutorException, CompilationErrorException, CompilationException {
        requireNonNull(sourceCode, "sourceCode of contract class is null");
        if (sourceCode.isEmpty()) throw new ContractExecutorException("sourceCode of contract class is empty");
        CompilationPackage compilationPackage = InMemoryCompiler.compileSourceCode(sourceCode);
        return GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
    }



    private Permissions createPermissions() {
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
//        permissions.add(new FilePermission("<<ALL FILES>>", "read"));
        return permissions;
    }


}
