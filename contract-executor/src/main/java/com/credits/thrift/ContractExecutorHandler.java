package com.credits.thrift;

import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ContractExecutor;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeMultipleResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.client.executor.thrift.generated.GetContractVariablesResult;
import com.credits.client.executor.thrift.generated.GetterMethodResult;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import org.apache.thrift.TUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.ioc.Injector.INJECTOR;
import static com.credits.utils.ContractExecutorServiceUtils.writeLog;
import static java.util.stream.Collectors.toList;

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
    public ExecuteByteCodeResult executeByteCode(long accessId, ByteBuffer initiatorAddress,
        SmartContractBinary invokedContract, String method, List<Variant> params, long executionTime, byte version) {
        writeLog("Start execute bytecode");
        logger.debug("\n--> executeByteCode(" + "\naccessId = {}," + "\naddress = {}," + "\nbyteCode length= {}, " +
                "\ncontractState length= {}, " + "\ncontractState hash= {} " + "\nmethod = {}, " + "\nparams = {}."
            , accessId, encodeToBASE58(initiatorAddress.array()),
            invokedContract.byteCodeObjects.size(), invokedContract.contractState.array().length,
            invokedContract.contractState.hashCode(), method,
            (params == null ? "no params" : params.stream().map(TUnion::toString).reduce("", String::concat)));

        Variant[] paramsArray = params == null ? null : params.toArray(new Variant[0]);
        ExecuteByteCodeResult result =
            new ExecuteByteCodeResult(new APIResponse(SUCCESS_CODE, "success"), null, null);/*todo what ? ?*/
        try {
            ReturnValue returnValue =
                service.execute(accessId, initiatorAddress.array(), invokedContract.contractAddress.array(),
                    GeneralConverter.byteCodeObjectsToByteCodeObjectsData(invokedContract.byteCodeObjects),
                    invokedContract.contractState.array(), method, new Variant[][] {paramsArray}, executionTime);
            result.invokedContractState = ByteBuffer.wrap(returnValue.getContractState());
            if (returnValue.getVariantsList() != null) {
                result.ret_val = returnValue.getVariantsList().get(0);
            }
            result.externalContractsState = returnValue.getExternalContractsState();
            logger.info("\n<--executeByteCode \ncontractState length= {}\ncontractState hash= {}\nresponse= {}" +
                    "\n-------------------", result.invokedContractState.array().length,
                result.invokedContractState.hashCode(), result);
        } catch (ContractExecutorException e) {
            result.setStatus(new APIResponse(ERROR_CODE, e.getMessage()));
            logger.info("\n<-- error executeByteCode  result \n{}", result);
        }
        logger.debug("\n-->execute  contractStateHash {} {}", Arrays.hashCode(result.getInvokedContractState()), result);
        writeLog("End execute bytecode");
        return result;
    }

    @Override
    public ExecuteByteCodeMultipleResult executeByteCodeMultiple(long accessId, ByteBuffer initiatorAddress,
        SmartContractBinary invokedContract, String method, List<List<Variant>> params, long executionTime,
        byte version) {
        writeLog("Start execute code multiple");
        Variant[][] paramsArray = null;
        if (params != null) {
            paramsArray = new Variant[params.size()][];
            for (int i = 0; i < params.size(); i++) {
                List<Variant> list = params.get(i);
                paramsArray[i] = list.toArray(new Variant[0]);
            }
        }

        logger.info(String.format(
            "\n--> executeByteCodeMultiple(\naddress = %s, \nbyteCode length= %d, \ncontractState length= %d, \ncontractState hash= %s \nmethod = %s, \nparams = %s.",
            encodeToBASE58(initiatorAddress.array()), invokedContract.getByteCodeObjects().size(),
            invokedContract.contractState.array().length, invokedContract.contractState.hashCode(), method,
            Arrays.toString(paramsArray)));

        ExecuteByteCodeMultipleResult response = new ExecuteByteCodeMultipleResult();
        try {
            ReturnValue returnValue =
                service.execute(accessId, initiatorAddress.array(), invokedContract.contractAddress.array(),
                    GeneralConverter.byteCodeObjectsToByteCodeObjectsData(invokedContract.getByteCodeObjects()),
                    invokedContract.contractState.array(), method, paramsArray, executionTime);
            List<GetterMethodResult> getterResults = new ArrayList<>(returnValue.getVariantsList().size());
            List<Variant> variantsList = returnValue.getVariantsList();
            List<APIResponse> statusesList = returnValue.getStatusesList();
            for (int i = 0; i < variantsList.size(); i++) {
                GetterMethodResult getterMethodResult = new GetterMethodResult(statusesList.get(i));
                getterMethodResult.setRet_val(variantsList.get(i));
                getterResults.add(getterMethodResult);
            }
            response.setResults(getterResults);
            response.setStatus(new APIResponse(SUCCESS_CODE, "success"));
        } catch (Throwable e) {
            response.setStatus(new APIResponse(ERROR_CODE, e.getMessage()));
        }
        logger.info("\n<--executeByteCodeMultiple\nresponse= {}", response);
        writeLog("End execute code multiple");
        return response;
    }


    @Override
    public GetContractMethodsResult getContractMethods(List<ByteCodeObject> compilationUnits, byte version) {
        writeLog("Start get contract methods");
        logger.debug("\n--> getContractMethods(\nbytecode = {} bytes)", compilationUnits.size());
        GetContractMethodsResult result = new GetContractMethodsResult();
        try {
            List<MethodDescriptionData> contractsMethods =
                service.getContractsMethods(GeneralConverter.byteCodeObjectTobyteCodeObjectData(compilationUnits));
            result.methods =
                contractsMethods.stream().map(GeneralConverter::convertMethodDataToMethodDescription).collect(toList());
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
        } catch (ContractExecutorException e) {
            result.setStatus(getErrorState(e.getMessage()));
        }
        logger.debug("\n<--getContractMethods {}", result);
        writeLog("End get contract methods");
        return result;
    }

    @Override
    public GetContractVariablesResult getContractVariables(List<ByteCodeObject> compilationUnits,
        ByteBuffer contractState, byte version) {
        writeLog("Start get contract variables");
        logger.debug("\n--> getContractVariables(\nbytecode = {} bytes,\n contractState = {} bytes)", compilationUnits.size(),
            contractState.array().length);
        GetContractVariablesResult result = new GetContractVariablesResult();
        try {
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
            result.setContractVariables(
                service.getContractVariables(GeneralConverter.byteCodeObjectTobyteCodeObjectData(compilationUnits),
                    contractState.array()));
        } catch (Throwable e) {
            result.setStatus(getErrorState(e.getMessage()));
        }
        logger.debug("\n<--getContractVariables  {}", result);
        writeLog("End get contract variables");
        return result;
    }


    private APIResponse getErrorState(String errorMessage) {
        return new APIResponse(ERROR_CODE, errorMessage);
    }

    @Override
    public CompileSourceCodeResult compileSourceCode(String sourceCode, byte version) {
        writeLog("Start compile sourcecode");
        logger.debug("\n--> compileBytecode(\nsourceCode = {})", sourceCode);
        CompileSourceCodeResult result = new CompileSourceCodeResult();
        try {
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
            result.setByteCodeObjects(
                GeneralConverter.byteCodeObjectsDataToByteCodeObjects(service.compileClass(sourceCode)));
        } catch (CompilationErrorException exception) {
            result.setStatus(getErrorState(exception.getErrors()
                .stream()
                .map(e -> "Error on line " + e.getLineNumber() + ": " + e.getErrorMessage())
                .collect(Collectors.joining("\n"))));
        } catch (Throwable e) {
            result.setStatus(getErrorState(e.getMessage()));
        }
        logger.debug("\n<--compileByteCode {}", result);
        writeLog("End compile sourcecode");
        return result;
    }

}
