package com.credits.thrift;

import com.credits.client.executor.thrift.generated.*;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.CompilationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.ReturnValue;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.util.GeneralConverter.*;
import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.thrift.utils.ContractExecutorUtils.validateVersion;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static com.credits.utils.ContractExecutorServiceUtils.failureApiResponse;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);

    private final ContractExecutorService ceService;

    @Inject
    public ContractExecutorHandler(ContractExecutorService contractExecutorService) {
        INJECTOR.component.inject(this);
        ceService = contractExecutorService;
    }

    @Override
    public ExecuteByteCodeResult executeByteCode(long accessId,
                                                 ByteBuffer initiatorAddress,
                                                 SmartContractBinary invokedContract,
                                                 List<MethodHeader> methodHeaders,
                                                 long executionTime,
                                                 short version) {
        ExecuteByteCodeResult executeByteCodeResult;
        try {
            logger.debug("<-- executeByteCode\n" +
                                 "accessId={}\n" +
                                 "initiatorAddress={}\n" +
                                 "invokedContract={}\n" +
                                 "methodHeaders={}\n" +
                                 "executionTime={}\n" +
                                 "version={}",
                         accessId, encodeToBASE58(initiatorAddress.array()), invokedContract, methodHeaders, executionTime, version);

            validateVersion(version);

            final var session = new ExecuteByteCodeSession(ceService, accessId, initiatorAddress, invokedContract, methodHeaders, executionTime);
            executeByteCodeResult = session.perform();
        } catch (Throwable e) {
            executeByteCodeResult = new ExecuteByteCodeResult(failureApiResponse(e), emptyList(), emptyMap());
        }

        logger.debug("--> {}", executeByteCodeResult);
        return executeByteCodeResult;
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
        ExecuteByteCodeMultipleResult byteCodeMultipleResult;

        try {
            logger.debug("<-- executeByteCodeMultiple" +
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

            Variant[][] paramsArray = null;
            if (params != null) {
                paramsArray = new Variant[params.size()][];
                for (int i = 0; i < params.size(); i++) {
                    List<Variant> list = params.get(i);
                    paramsArray[i] = list.toArray(new Variant[0]);
                }
            }

            byteCodeMultipleResult = new ExecuteByteCodeMultipleResult(SUCCESS_API_RESPONSE, null);
            validateVersion(version);

            ReturnValue returnValue =
                    classObject.instance == null || classObject.instance.array().length == 0
                            ? ceService.deploySmartContract(new DeployContractSession(accessId,
                                                                                      encodeToBASE58(initiatorAddress.array()),
                                                                                      encodeToBASE58(invokedContract.contractAddress.array()),
                                                                                      byteCodeObjectsToByteCodeObjectsData(classObject.byteCodeObjects),
                                                                                      executionTime))
                            : ceService.executeSmartContract(new InvokeMethodSession(accessId,
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
            byteCodeMultipleResult = new ExecuteByteCodeMultipleResult(failureApiResponse(e), emptyList());
        }

        logger.debug("--> {}", byteCodeMultipleResult);
        return byteCodeMultipleResult;
    }

    @Override
    public GetContractMethodsResult getContractMethods(List<ByteCodeObject> compilationUnits, short version) {
        GetContractMethodsResult result;
        try {
            logger.debug("<-- getContractMethods(\n" +
                                 "bytecode = {} bytes, \n" +
                                 "version = {})",
                         compilationUnits.size(),
                         version);
            validateVersion(version);
            List<MethodDescriptionData> contractsMethods =
                    ceService.getContractsMethods(byteCodeObjectToByteCodeObjectData(compilationUnits));

            result = new GetContractMethodsResult(SUCCESS_API_RESPONSE,
                                                  contractsMethods.stream().map(GeneralConverter::convertMethodDataToMethodDescription).collect(toList()));
        } catch (Throwable e) {
            result = new GetContractMethodsResult(failureApiResponse(e), emptyList());
        }
        logger.debug("--> {}", result);
        return result;
    }

    @Override
    public GetContractVariablesResult getContractVariables(
            List<ByteCodeObject> compilationUnits,
            ByteBuffer contractState,
            short version) {
        GetContractVariablesResult result;
        try {
            logger.debug("<-- getContractVariables(\n" +
                                 "bytecode = {} bytes, \n" +
                                 "contractState = {} bytes, \n" +
                                 "version = {})",
                         compilationUnits.size(), contractState.array().length, version);
            validateVersion(version);
            result = new GetContractVariablesResult(SUCCESS_API_RESPONSE,
                                                    ceService.getContractVariables(
                                                            byteCodeObjectToByteCodeObjectData(compilationUnits),
                                                            contractState.array()));
        } catch (Throwable e) {
            result = new GetContractVariablesResult(failureApiResponse(e), emptyMap());
        }
        logger.debug("--> {}", result);
        return result;
    }


    @Override
    public CompileSourceCodeResult compileSourceCode(String sourceCode, short version) {
        CompileSourceCodeResult result;
        try {
            logger.debug("<-- compileBytecode(\nsourceCode = {},\nversion = {})", sourceCode, version);
            validateVersion(version);
            result = new CompileSourceCodeResult(SUCCESS_API_RESPONSE, byteCodeObjectsDataToByteCodeObjects(ceService.compileClass(sourceCode)));
        } catch (CompilationException exception) {
            result = new CompileSourceCodeResult();
            result.setStatus(new APIResponse(
                    FAILURE.code,
                    exception.getMessage()
            ));
        } catch (Throwable e) {
            result = new CompileSourceCodeResult(failureApiResponse(e), emptyList());
        }
        logger.debug("--> {}", result);
        return result;
    }
}
