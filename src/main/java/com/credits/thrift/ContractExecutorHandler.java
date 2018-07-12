package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.thrift.generated.APIResponse;
import com.credits.thrift.generated.ContractExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.ByteBuffer;
import java.util.List;

@Component
public class ContractExecutorHandler implements ContractExecutor.Iface {

    @Resource
    private ContractExecutorService service;

    @Override
    public APIResponse executeByteCode(String address, ByteBuffer byteCode, ByteBuffer contractState, String method,
                                       List<String> params) {
        String[] paramsArray = params == null ? null : params.toArray(new String[0]);

        APIResponse response = new APIResponse((byte) 0, "", contractState, null, null);
        try {
            ReturnValue returnValue = service.execute(address, byteCode.array(), contractState.array(), method, paramsArray);
            response.contractState = ByteBuffer.wrap(returnValue.getContractState());
            response.ret_val = returnValue.getVariant();
            response.contractVariables = returnValue.getContractVariables();
        } catch (ContractExecutorException e) {
            response.setCode((byte) 1);
            response.setMessage(e.getMessage());
            return response;
        }
        return response;
    }
}
