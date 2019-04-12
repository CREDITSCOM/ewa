package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ContractExecutor;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeMultipleResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.client.executor.thrift.generated.GetContractVariablesResult;
import com.credits.client.executor.thrift.generated.GetterMethodResult;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.CompilationException;
import org.apache.thrift.TUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.ReturnValue;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.credits.general.util.GeneralConverter.byteCodeObjectsToByteCodeObjectsData;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.thrift.utils.ContractExecutorUtils.validateVersion;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static com.credits.utils.ContractExecutorServiceUtils.failureApiResponse;
import static java.util.stream.Collectors.toList;

public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);

    @Inject
    ContractExecutorService service;

    ContractExecutorHandler() {
        INJECTOR.component.inject(this);
    }

    @Override
    public ExecuteByteCodeResult executeByteCode(
        long accessId,
        ByteBuffer initiatorAddress,
        SmartContractBinary invokedContract,
        String method,
        List<Variant> params,
        long executionTime,
            short version) {

        ClassObject classObject = invokedContract.object;

        logger.debug(
            "\n" +
                "\n<-- executeByteCode(" +
                "\naccessId = {}," +
                "\naddress = {}," +
                "\nobject.byteCodeObjects length= {}, " +
                "\nobject.instance length= {}, " +
                "\nobject.instance hash= {} " +
                "\nmethod = {}, " +
                "\nparams = {}, " +
                "\nversion = {}.",
            accessId,
            encodeToBASE58(initiatorAddress.array()),
            (classObject != null && classObject.byteCodeObjects != null ? classObject.byteCodeObjects.size() : "null"),
            (classObject != null && classObject.instance != null ? classObject.instance.position() : "null"),
            (classObject != null && classObject.instance != null ? classObject.instance.hashCode() : "null"),
            method,
            (params == null ? "no params" : params.stream().map(TUnion::toString).reduce("", String::concat)),
            version
        );

        Objects.requireNonNull(classObject, "class object can't be null");
        validateVersion(version);

        Variant[] paramsArray = params == null ? null : params.toArray(new Variant[0]);
        ExecuteByteCodeResult result = new ExecuteByteCodeResult(null, null, null);
        try {
            // TODO: 3/18/2019 execute smart contract must be throw exception if contract state is empty or null
            ReturnValue returnValue =
                isDeployTransaction(classObject, method) ?
                    service.deploySmartContract(new DeployContractSession(
                        accessId,
                        encodeToBASE58(initiatorAddress.array()),
                        encodeToBASE58(invokedContract.contractAddress.array()),
                        byteCodeObjectsToByteCodeObjectsData(classObject.byteCodeObjects),
                        executionTime))
                    :
                        service.executeSmartContract(new InvokeMethodSession(
                            accessId,
                            encodeToBASE58(initiatorAddress.array()),
                            encodeToBASE58(invokedContract.contractAddress.array()),
                            byteCodeObjectsToByteCodeObjectsData(classObject.byteCodeObjects),
                            classObject.instance.array(),
                            method,
                            new Variant[][] {paramsArray},
                            executionTime));


            result.invokedContractState = ByteBuffer.wrap(returnValue.newContractState);
            if (returnValue.executeResults != null) {
                result.status = returnValue.executeResults.get(0).status;
                result.ret_val = returnValue.executeResults.get(0).result;
            }

            if (returnValue.externalSmartContracts != null) {
                result.externalContractsState = returnValue.externalSmartContracts.keySet().stream().reduce(
                    new HashMap<>(),
                    (newMap, address) -> {
                        if (!Arrays.equals(decodeFromBASE58(address), invokedContract.contractAddress.array())) {
                            newMap.put(
                                ByteBuffer.wrap(decodeFromBASE58(address)),
                                ByteBuffer.wrap(returnValue.externalSmartContracts.get(address).contractData.contractState));
                        }
                        return newMap;
                    },
                    (map1, map2) -> map1);
            }

            logger.debug("\nexecuteByteCode success --> contractStateHash {} {}", Arrays.hashCode(result.getInvokedContractState()), result);

        } catch (Throwable e) {
//            return null;
            result.status = failureApiResponse(e);
            logger.debug("\nexecuteByteCode error --> {}", result);
        }
        return result;
    }

    @Override
    public ExecuteByteCodeMultipleResult executeByteCodeMultiple(
        long accessId,
        ByteBuffer initiatorAddress,
        SmartContractBinary invokedContract,
        String method,
        List<List<Variant>> params,
        long executionTime,
        short version) {

        ClassObject classObject = invokedContract.object;

        logger.debug(
            "\n<-- executeByteCodeMultiple(" +
                "\naccessId = {}," +
                "\naddress = {}," +
                "\nobject.byteCodeObjects length= {}, " +
                "\nobject.instance length= {}, " +
                "\nobject.instance hash= {} " +
                "\nmethod = {}, " +
                "\nparams = {}, " +
                "\nversion = {}.",
            accessId,
            encodeToBASE58(initiatorAddress.array()),
            (classObject != null && classObject.byteCodeObjects != null ? classObject.byteCodeObjects.size() : "null"),
            (classObject != null && classObject.instance != null ? classObject.instance.position() : "null"),
            (classObject != null && classObject.instance != null ? classObject.instance.hashCode() : "null"),
            method,
            params == null ? "no params" : params.toString(),
            version
        );

        Objects.requireNonNull(classObject, "class object can't be null");
        validateVersion(version);

        Variant[][] paramsArray = null;
        if (params != null) {
            paramsArray = new Variant[params.size()][];
            for (int i = 0; i < params.size(); i++) {
                List<Variant> list = params.get(i);
                paramsArray[i] = list.toArray(new Variant[0]);
            }
        }

        ExecuteByteCodeMultipleResult byteCodeMultipleResult = new ExecuteByteCodeMultipleResult(SUCCESS_API_RESPONSE, null);
        try {
            ReturnValue returnValue =
                isDeployTransaction(classObject, method) ?
                    service.deploySmartContract(new DeployContractSession(
                        accessId,
                        encodeToBASE58(initiatorAddress.array()),
                        encodeToBASE58(invokedContract.contractAddress.array()),
                        byteCodeObjectsToByteCodeObjectsData(classObject.byteCodeObjects),
                        executionTime))
                    :
                        service.executeSmartContract(new InvokeMethodSession(
                            accessId,
                            encodeToBASE58(initiatorAddress.array()),
                            encodeToBASE58(invokedContract.contractAddress.array()),
                            byteCodeObjectsToByteCodeObjectsData(classObject.byteCodeObjects),
                            classObject.instance.array(),
                            method,
                            paramsArray,
                            executionTime));

            byteCodeMultipleResult.results = returnValue.executeResults.stream().map(rv -> {
                final GetterMethodResult getterMethodResult = new GetterMethodResult(rv.status);
                getterMethodResult.ret_val = rv.result;
                return getterMethodResult;
            }).collect(Collectors.toList());
        } catch (Throwable e) {
            byteCodeMultipleResult.setStatus(failureApiResponse(e));
            logger.debug("\nexecuteByteCodeMultiple error --> {}", byteCodeMultipleResult);
        }
        logger.debug("\nexecuteByteCodeMultiple success --> {}", byteCodeMultipleResult);
        return byteCodeMultipleResult;
    }

    @Override
    public GetContractMethodsResult getContractMethods(List<ByteCodeObject> compilationUnits, short version) {
        logger.debug("\n<-- getContractMethods(\nbytecode = {} bytes, \nversion = {})", compilationUnits.size(), version);
        validateVersion(version);
        GetContractMethodsResult result = new GetContractMethodsResult();
        try {
            List<MethodDescriptionData> contractsMethods =
                service.getContractsMethods(GeneralConverter.byteCodeObjectToByteCodeObjectData(compilationUnits));
            result.methods =
                contractsMethods.stream().map(GeneralConverter::convertMethodDataToMethodDescription).collect(toList());
            result.setStatus(SUCCESS_API_RESPONSE);
        } catch (Throwable e) {
            result.setStatus(failureApiResponse(e));
            logger.debug("\ngetContractMethods error --> {}", result);
        }
        logger.debug("\ngetContractMethods success --> {}", result);
        return result;
    }

    @Override
    public GetContractVariablesResult getContractVariables(
        List<ByteCodeObject> compilationUnits,
        ByteBuffer contractState,
        short version) {
        logger.debug("\n<-- getContractVariables(\nbytecode = {} bytes, \ncontractState = {} bytes, \nversion = {})",
                     compilationUnits.size(), contractState.array().length, version);
        validateVersion(version);
        GetContractVariablesResult result = new GetContractVariablesResult();
        try {
            result.setStatus(SUCCESS_API_RESPONSE);
            result.setContractVariables(service.getContractVariables(
                GeneralConverter.byteCodeObjectToByteCodeObjectData(compilationUnits),
                contractState.array()));
        } catch (Throwable e) {
            result.setStatus(failureApiResponse(e));
            logger.debug("\ngetContractVariables error --> {}", result);
        }
        logger.debug("\ngetContractVariables success --> {}", result);
        return result;
    }


    @Override
    public CompileSourceCodeResult compileSourceCode(String sourceCode, short version) {
        logger.debug("\n<-- compileBytecode(sourceCode = {}, \nversion = {})", sourceCode, version);
        validateVersion(version);
        CompileSourceCodeResult result = new CompileSourceCodeResult();
        try {
            result.setStatus(SUCCESS_API_RESPONSE);
            result.setByteCodeObjects(
                GeneralConverter.byteCodeObjectsDataToByteCodeObjects(service.compileClass(sourceCode)));
        } catch (CompilationException exception) {
            result.setStatus(new APIResponse(
                ApiResponseCode.FAILURE.code,
                exception.getErrors()
                    .stream()
                    .map(e -> "Error on line " + e.getLineNumber() + ": " + e.getErrorMessage())
                    .collect(Collectors.joining("\n"))));
        } catch (Throwable e) {
            result.setStatus(failureApiResponse(e));
            logger.debug("\ncompileByteCode error --> {}", result);
        }
        logger.debug("\ncompileByteCode success --> {}", result);
        return result;
    }

    private boolean isDeployTransaction(ClassObject classObject, String method) {
        return method.isEmpty() && (classObject == null || classObject.instance == null || classObject.instance.position() == 0);
    }
}
