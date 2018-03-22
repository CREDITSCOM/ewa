package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.usercode.UserCodeStorageService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
        MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, null, fileContent);
        APIResponse response = new APIResponse((byte) 0, "");
        try {
            storageService.store(multipartFile, address);
            service.execute(address);
        } catch (ContractExecutorException e) {
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
