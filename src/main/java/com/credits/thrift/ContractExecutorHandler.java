package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.thrift.generated.APIResponse;
import com.credits.thrift.generated.ContractExecutor;
import com.credits.thrift.generated.GetContractMethodsResult;
import com.credits.thrift.generated.Variant;
import org.apache.thrift.TUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.ByteBuffer;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);

    @Resource
    private ContractExecutorService service;

    @Override
    public APIResponse executeByteCode(ByteBuffer address, ByteBuffer byteCode, ByteBuffer contractState, String method,
                                       List<Variant> params) {
        Variant[] paramsArray = params == null ? null : params.toArray(new Variant[0]);

        logger.debug(String.format("<-- execute(\naddress = %s, \nbyteCode length= %d, \ncontractState length= %d, \nmethod = %s, \nparams = %s.",
            address, byteCode.array().length, contractState.array().length, method, (params == null ? "no params" : params.stream().map(TUnion::toString).reduce("", String::concat))));

        APIResponse response = new APIResponse((byte) 0, "", contractState);
        try {
            ReturnValue returnValue = service.execute(address.array(), byteCode.array(), contractState.array(), method, paramsArray);
            response.contractState = ByteBuffer.wrap(returnValue.getContractState());
            response.ret_val = returnValue.getVariant();
            response.contractVariables = returnValue.getContractVariables();
        } catch (ContractExecutorException e) {
            response.setCode((byte) 1);
            response.setMessage(e.getMessage());
        }
        logger.debug("--> " + response);
        return response;
    }

    @Override
    public GetContractMethodsResult getContractMethods(ByteBuffer bytecode) {
        logger.debug("<-- getContractMethods(bytecode = " + bytecode.array().length + " bytes)");
        GetContractMethodsResult result = new GetContractMethodsResult();
        try {
            List<MethodDescription> contractsMethods = service.getContractsMethods(bytecode.array());
            result.methods = contractsMethods.stream().map( it -> new com.credits.thrift.generated.MethodDescription(it.name, it.argTypes, it.returnType)).collect(toList());
        } catch (ContractExecutorException e) {
          result.setCode((byte) 1);
          result.setMessage(e.getMessage());
        }
        logger.debug("--> " + result);
        return result;
    }
}
