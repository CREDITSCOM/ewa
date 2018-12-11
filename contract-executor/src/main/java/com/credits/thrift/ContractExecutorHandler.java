package com.credits.thrift;

import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ContractExecutor;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeMultipleResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.client.executor.thrift.generated.GetContractVariablesResult;
import com.credits.client.executor.thrift.generated.GetterMethodResult;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.MethodDescription;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import org.apache.thrift.TException;
import org.apache.thrift.TUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.credits.general.util.Converter.encodeToBASE58;
import static java.util.stream.Collectors.toList;

public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);
    public final static byte ERROR_CODE = (byte) 1;
    public final static byte SUCCESS_CODE = (byte) 0;

    private ContractExecutorService service;

    ContractExecutorHandler(){
        service = new ContractExecutorServiceImpl();
    }

    @Override
    public ExecuteByteCodeResult executeByteCode(ByteBuffer address, ByteBuffer byteCode, ByteBuffer contractState,
        String method, List<Variant> params, long executionTime) {
        logger.debug("<-- execute(" +
                    "\naddress = {}," +
                    "\nbyteCode length= {}, " +
                    "\ncontractState length= {}, " +
                    "\ncontractState hash= {} " +
                    "\nmethod = {}, " +
                    "\nparams = {}.",
            encodeToBASE58(address.array()), byteCode.array().length, contractState.array().length, contractState.hashCode(),
            method, (params == null ? "no params" : params.stream().map(TUnion::toString).reduce("", String::concat)));

        Variant[] paramsArray = params == null ? null : params.toArray(new Variant[0]);
        ExecuteByteCodeResult result = new ExecuteByteCodeResult(new APIResponse(SUCCESS_CODE, "success"), null);
        try {
            ReturnValue returnValue = service.execute(address.array(), byteCode.array(), contractState.array(), method, new Variant[][] {paramsArray},executionTime);
            result.contractState = ByteBuffer.wrap(returnValue.getContractState());
            if (returnValue.getVariantsList() != null) {
                result.ret_val = returnValue.getVariantsList().get(0);
            }
            logger.info("executeByteCode -->\ncontractState length= {}\ncontractState hash= {}\nresponse= {}",
                result.contractState.array().length, contractState.hashCode(), result);
        } catch (ContractExecutorException e) {
            result.setStatus(new APIResponse(ERROR_CODE, e.getMessage()));
            logger.info("executeByteCode --> {}", result);
        }
        logger.debug("execute --> contractStateHash {} {}", Arrays.hashCode(result.getContractState()), result);
        return result;
    }


    @Override
    public ExecuteByteCodeMultipleResult executeByteCodeMultiple(ByteBuffer address, ByteBuffer bytecode,
        ByteBuffer contractState, String method, List<List<Variant>> params, long executionTime) {

        Variant[][] paramsArray = null;
        if (params != null) {
            paramsArray = new Variant[params.size()][];
            for (int i = 0; i < params.size(); i++) {
                List<Variant> list = params.get(i);
                paramsArray[i] = list.toArray(new Variant[0]);
            }
        }

        logger.info(String.format(
            "<-- executeByteCodeMultiple(\naddress = %s, \nbyteCode length= %d, \ncontractState length= %d, \ncontractState hash= %s \nmethod = %s, \nparams = %s.",
            encodeToBASE58(address.array()), bytecode.array().length, contractState.array().length,
            contractState.hashCode(), method, Arrays.toString(paramsArray)));

        ExecuteByteCodeMultipleResult response = new ExecuteByteCodeMultipleResult();
        try {
            ReturnValue returnValue =
                service.execute(address.array(), bytecode.array(), contractState.array(), method, paramsArray,
                    executionTime);
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
        logger.info("executeByteCodeMultiple -->\nresponse= {}", response);

        return response;
    }


    @Override
    public GetContractMethodsResult getContractMethods(ByteBuffer bytecode) {
        logger.debug("<-- getContractMethods(bytecode = {} bytes)", bytecode.array().length);
        GetContractMethodsResult result = new GetContractMethodsResult();
        try {
            List<MethodDescriptionData> contractsMethods = service.getContractsMethods(bytecode.array());
            result.methods = contractsMethods.stream().map( it -> new MethodDescription(it.returnType, it.name, it.args)).collect(toList());
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
        } catch (ContractExecutorException e) {
            result.setStatus(getErrorState(e.getMessage()));
        }
        logger.debug("getContractMethods --> {}", result);
        return result;
    }

    @Override
    public GetContractVariablesResult getContractVariables(ByteBuffer byteCode, ByteBuffer contractState) {
        logger.debug("<-- getContractVariables(bytecode = {} bytes, contractState = {} bytes)", byteCode.array().length,
            contractState.array().length);
        GetContractVariablesResult result = new GetContractVariablesResult();
        try {
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
            result.setContractVariables(service.getContractVariables(byteCode.array(), contractState.array()));
        } catch (Throwable e) {
            result.setStatus(getErrorState(e.getMessage()));
        }
        logger.debug("getContractVariables --> {}", result);
        return result;
    }


    private APIResponse getErrorState(String errorMessage) {
        return new APIResponse(ERROR_CODE, errorMessage);
    }

    @Override
    public CompileSourceCodeResult compileSourceCode(String sourceCode) throws TException {
        logger.debug("<-- compileBytecode(sourceCode = {})", sourceCode);
        CompileSourceCodeResult result = new CompileSourceCodeResult();
        try {
            result.setStatus(new APIResponse(SUCCESS_CODE, "success"));
            result.setByteCode(service.compileClass(sourceCode));
        }catch (CompilationErrorException exception){
            result.setStatus(getErrorState(exception.getErrors().stream()
                .map(e -> "Error on line " + e.getLineNumber() + ": " + e.getErrorMessage())
                .collect(Collectors.joining("\n"))));
        } catch (Throwable e) {
            result.setStatus(getErrorState(e.getMessage()));
        }
        logger.debug("compileByteCode --> {}", result);
        return result;
    }
}
