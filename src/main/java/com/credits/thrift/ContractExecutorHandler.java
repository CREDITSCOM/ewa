package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

@Component
public class ContractExecutorHandler implements ContractExecutor.Iface {

    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";
    private final static String SER_TEMP_FOLDER_PATH = File.separator + "temp" + File.separator;

    @Resource
    private UserCodeStorageService storageService;

    @Resource
    private ContractExecutorService service;

    @Override
    public APIResponse executeByteCode(String address, ByteBuffer byteCode, ByteBuffer contractState, String method,
        List<String> params) throws TException {
        String[] paramsArray = params == null ? null : params.toArray(new String[0]);

        APIResponse response = new APIResponse((byte) 0, "", contractState);
        try {
            response.contractState = ByteBuffer.wrap(service.execute(address, byteCode.array(), null, method, paramsArray));
        } catch (ContractExecutorException e) {
            response.setCode((byte) 1);
            response.setMessage(e.getMessage());
            return response;
        }
        return response;
    }
}
