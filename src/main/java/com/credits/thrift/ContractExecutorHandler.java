package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.thrift.generated.APIResponse;
import com.credits.thrift.generated.ContractExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorHandler.class);

    @Resource
    private ContractExecutorService service;

    @Override
    public APIResponse executeByteCode(String address, ByteBuffer byteCode, ByteBuffer contractState, String method,
                                       List<String> params) {
        String[] paramsArray = params == null ? null : params.toArray(new String[0]);

        logger.info(String.format("Executing contract:\nAddress = %s, \nByteCode = %s, \nContractState = %s, \nMethod = %s, \nParams = %s.",
            address, Arrays.toString(byteCode.array()), Arrays.toString(contractState.array()),
            method, Optional.ofNullable(params).orElse(Collections.singletonList("no params")).stream().collect(Collectors.joining())));

        APIResponse response = new APIResponse((byte) 0, "", contractState);
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
