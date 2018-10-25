package com.credits.thrift;

import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.client.executor.thrift.APIResponse;
import com.credits.client.executor.thrift.ContractExecutor;
import com.credits.client.executor.thrift.GetContractMethodsResult;
import com.credits.client.executor.thrift.MethodDescription;
import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generate.Variant;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import org.apache.thrift.TUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

import static com.credits.general.util.Converter.encodeToBASE58;
import static java.util.stream.Collectors.toList;

public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);

    private ContractExecutorService service;

    public ContractExecutorHandler(){
        service = new ContractExecutorServiceImpl();
    }

    @Override
    public APIResponse executeByteCode(ByteBuffer address, ByteBuffer byteCode, ByteBuffer contractState, String method,
                                       List<Variant> params) {
        logger.debug(String.format("<-- execute(\naddress = %s, \nbyteCode length= %d, \ncontractState length= %d, \nmethod = %s, \nparams = %s.",
            encodeToBASE58(address.array()), byteCode.array().length, contractState.array().length, method, (params == null ? "no params" : params.stream().map(TUnion::toString).reduce("", String::concat))));

        Variant[] paramsArray = params == null ? null : params.toArray(new Variant[0]);

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
            List<MethodDescriptionData> contractsMethods = service.getContractsMethods(bytecode.array());
            result.methods = contractsMethods.stream().map( it -> new MethodDescription(it.name, it.argTypes, it.returnType)).collect(toList());
        } catch (ContractExecutorException e) {
          result.setCode((byte) 1);
          result.setMessage(e.getMessage());
        }
        logger.debug("--> " + result);
        return result;
    }
}
