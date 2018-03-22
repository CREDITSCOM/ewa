package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class ContractExecutorHandler implements ContractExecutor.Iface {

    @Resource
    private UserCodeStorageService storageService;

    @Resource
    private ContractExecutorService service;


    @Override
    public APIResponse store(ContractFile file, String address) {
        String fileName = file.getName();
        byte[] fileContent = file.getFile();
        File sourceFile = new File(fileName);

        APIResponse response = new APIResponse((byte) 0, "");
        try {
            FileUtils.writeByteArrayToFile(sourceFile, fileContent);
            storageService.store(sourceFile, address);
            sourceFile.delete();
            service.execute(address);
        } catch (ContractExecutorException|IOException e) {
            response.setCode((byte) 1);
            response.setMessage(e.getMessage());
            return response;
        }
        return response;
    }

    @Override
    public APIResponse execute(String address, String method, List<String> params) {
        String[] paramsArray = params == null ? null : params.toArray(new String[0]);
        APIResponse response = new APIResponse((byte) 0, "");
        try {
            service.execute(address, method, paramsArray);
        } catch (ContractExecutorException e) {
            response.setCode((byte) 1);
            response.setMessage(e.getMessage());
            return response;
        }
        return response;
    }
}
