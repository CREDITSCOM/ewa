package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
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

        APIResponse response = new APIResponse((byte) 0, "", contractState);
        try {
            response.contractState =
                ByteBuffer.wrap(service.execute(address, byteCode.array(), contractState.array(), method, paramsArray));
        } catch (ContractExecutorException e) {
            response.setCode((byte) 1);
            response.setMessage(e.getMessage());
            return response;
        }
        return response;
    }
}
