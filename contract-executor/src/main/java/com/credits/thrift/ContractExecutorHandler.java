package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ContractExecutor;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeMultipleResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.client.executor.thrift.generated.GetContractVariablesResult;
import com.credits.client.executor.thrift.generated.GetterMethodResult;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.contract.session.DeployContractSession;
import com.credits.service.contract.session.InvokeMethodSession;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import org.apache.thrift.TUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.general.util.GeneralConverter.byteCodeObjectsToByteCodeObjectsData;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.ioc.Injector.INJECTOR;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);
    public final static byte ERROR_CODE = (byte) 1;
    public final static byte SUCCESS_CODE = (byte) 0;

    ContractExecutorService service;

    @Inject
    public NodeApiExecInteractionService dbInteractionService;

    ContractExecutorHandler() {
        INJECTOR.component.inject(this);
        service = new ContractExecutorServiceImpl(dbInteractionService);
    }


    @Override
    public ExecuteByteCodeResult executeByteCode(
        long accessId,
        ByteBuffer initiatorAddress,
        SmartContractBinary invokedContract,
        String method,
        List<Variant> params,
        long executionTime,
        byte version) {

        logger.debug(
            "\n<-- executeByteCode(" +
                "\naccessId = {}," +
                "\naddress = {}," +
                "\nbyteCode length= {}, " +
                "\ncontractState length= {}, " +
                "\ncontractState hash= {} " +
                "\nmethod = {}, " +
                "\nparams = {}.",
            accessId,
            encodeToBASE58(initiatorAddress.array()),
            invokedContract.byteCodeObjects.size(),
            invokedContract.contractState.array().length,
            invokedContract.contractState.hashCode(),
            method,
            (params == null ? "no params" : params.stream().map(TUnion::toString).reduce("", String::concat)));

        Variant[] paramsArray = params == null ? null : params.toArray(new Variant[0]);
        ExecuteByteCodeResult result = new ExecuteByteCodeResult(new APIResponse(SUCCESS_CODE, "success"), null, null);/*todo what ? ?*/
        try {
            ReturnValue returnValue =
                method.isEmpty() && invokedContract.contractState == null || invokedContract.contractState.array().length == 0 ?
                    service.deploySmartContract(new DeployContractSession(
                        accessId,
                        encodeToBASE58(initiatorAddress.array()),
                        encodeToBASE58(invokedContract.contractAddress.array()),
                        byteCodeObjectsToByteCodeObjectsData(invokedContract.byteCodeObjects),
                        executionTime))
                    :
                        service.executeSmartContract(new InvokeMethodSession(
                            accessId,
                            encodeToBASE58(initiatorAddress.array()),
                            encodeToBASE58(invokedContract.contractAddress.array()),
                            byteCodeObjectsToByteCodeObjectsData(invokedContract.byteCodeObjects),
                            invokedContract.contractState.array(),
                            method,
                            new Variant[][] {paramsArray},
                            executionTime));


            result.invokedContractState = ByteBuffer.wrap(returnValue.newContractState);
            if (returnValue.executeResults != null) {
                result.status = returnValue.executeResults.get(0).status;
                result.ret_val = returnValue.executeResults.get(0).result;
            }


            if (returnValue.externalContractStates != null) {
                result.externalContractsState = returnValue.externalContractStates.keySet().stream().reduce(
                    new HashMap<>(),
                    (newMap, address) -> {
                        newMap.put(ByteBuffer.wrap(decodeFromBASE58(address)), returnValue.externalContractStates.get(address));
                        return newMap;
                    },
                    (map1, map2) -> map1);
            }

            logger.debug("\nexecuteByteCode success --> contractStateHash {} {}", Arrays.hashCode(result.getInvokedContractState()), result);
        } catch (Throwable e) {
            result.setStatus(new APIResponse(ERROR_CODE, e.getMessage()));
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
        byte version) {

        logger.debug(
            "\n<-- executeByteCodeMultiple(" +
                "\naccessId = {}," +
                "\naddress = {}," +
                "\nbyteCode length= {}, " +
                "\ncontractState length= {}, " +
                "\ncontractState hash= {} " +
                "\nmethod = {}, " +
                "\nparams = {}.",
            accessId,
            encodeToBASE58(initiatorAddress.array()),
            invokedContract.byteCodeObjects.size(),
            invokedContract.contractState.array().length,
            invokedContract.contractState.hashCode(),
            method,
            params == null ? "no params" : params.toString());
        Variant[][] paramsArray = null;
        if (params != null) {
            paramsArray = new Variant[params.size()][];
            for (int i = 0; i < params.size(); i++) {
                List<Variant> list = params.get(i);
                paramsArray[i] = list.toArray(new Variant[0]);
            }
        }

        ExecuteByteCodeMultipleResult byteCodeMultipleResult = new ExecuteByteCodeMultipleResult(new APIResponse(SUCCESS_CODE, "success"), null);
        try {
            ReturnValue returnValue =
                method.isEmpty() && invokedContract.contractState == null || invokedContract.contractState.array().length == 0 ?
                    service.deploySmartContract(new DeployContractSession(
                        accessId,
                        encodeToBASE58(initiatorAddress.array()),
                        encodeToBASE58(invokedContract.contractAddress.array()),
                        byteCodeObjectsToByteCodeObjectsData(invokedContract.byteCodeObjects),
                        executionTime))
                    :
                        service.executeSmartContract(new InvokeMethodSession(
                            accessId,
                            encodeToBASE58(initiatorAddress.array()),
                            encodeToBASE58(invokedContract.contractAddress.array()),
                            byteCodeObjectsToByteCodeObjectsData(invokedContract.byteCodeObjects),
                            invokedContract.contractState.array(),
                            method,
                            paramsArray,
                            executionTime));

            byteCodeMultipleResult.results = returnValue.executeResults.stream().map(rv -> {
                final GetterMethodResult getterMethodResult = new GetterMethodResult(rv.status);
                getterMethodResult.ret_val = rv.result;
                return getterMethodResult;
            }).collect(Collectors.toList());
        } catch (Throwable e) {
            byteCodeMultipleResult.setStatus(new APIResponse(ERROR_CODE, getRootCauseMessage(e)));
            logger.debug("\nexecuteByteCodeMultiple error --> {}", byteCodeMultipleResult);
        }
        logger.debug("\nexecuteByteCodeMultiple success --> {}", byteCodeMultipleResult);
        return byteCodeMultipleResult;
    }

    @Override
    public GetContractMethodsResult getContractMethods(List<ByteCodeObject> compilationUnits, byte version) {
        logger.debug("\n<-- getContractMethods(\nbytecode = {} bytes)", compilationUnits.size());
        GetContractMethodsResult result = new GetContractMethodsResult();
        try {
            List<MethodDescriptionData> contractsMethods =
                service.getContractsMethods(GeneralConverter.byteCodeObjectTobyteCodeObjectData(compilationUnits));
            result.methods =
                contractsMethods.stream().map(GeneralConverter::convertMethodDataToMethodDescription).collect(toList());
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
        } catch (Throwable e) {
            result.setStatus(getErrorState(getRootCauseMessage(e)));
            logger.debug("\ngetContractMethods error --> {}", result);
        }
        logger.debug("\ngetContractMethods success --> {}", result);
        return result;
    }

    @Override
    public GetContractVariablesResult getContractVariables(
        List<ByteCodeObject> compilationUnits,
        ByteBuffer contractState,
        byte version) {
        logger.debug("\n<-- getContractVariables(\nbytecode = {} bytes,\n contractState = {} bytes)",
                     compilationUnits.size(), contractState.array().length);
        GetContractVariablesResult result = new GetContractVariablesResult();
        try {
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
            result.setContractVariables(service.getContractVariables(
                GeneralConverter.byteCodeObjectTobyteCodeObjectData(compilationUnits),
                contractState.array()));
        } catch (Throwable e) {
            result.setStatus(getErrorState(getRootCauseMessage(e)));
            logger.debug("\ngetContractVariables error --> {}", result);
        }
        logger.debug("\ngetContractVariables success --> {}", result);
        return result;
    }


    @Override
    public CompileSourceCodeResult compileSourceCode(String sourceCode, byte version) {
        logger.debug("\n<-- compileBytecode(sourceCode = {})", sourceCode);
        CompileSourceCodeResult result = new CompileSourceCodeResult();
        try {
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
            result.setByteCodeObjects(
                GeneralConverter.byteCodeObjectsDataToByteCodeObjects(service.compileClass(sourceCode)));
        } catch (CompilationErrorException exception) {
            result.setStatus(getErrorState(
                exception.getErrors()
                    .stream()
                    .map(e -> "Error on line " + e.getLineNumber() + ": " + e.getErrorMessage())
                    .collect(Collectors.joining("\n"))));
        } catch (Throwable e) {
            result.setStatus(getErrorState(getRootCauseMessage(e)));
            logger.debug("\ncompileByteCode error --> {}", result);
        }
        logger.debug("\ncompileByteCode success --> {}", result);
        return result;
    }

    private APIResponse getErrorState(String errorMessage) {
        return new APIResponse(ERROR_CODE, errorMessage);
    }
}
