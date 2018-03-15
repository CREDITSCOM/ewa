package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.thrift.TException;
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
    public void store(ContractFile file, String address) throws TException {
        String fileName = file.getName();
        byte[] fileContent = file.getFile();
        MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, null, fileContent);
        try {
            storageService.store(multipartFile, address);
            service.execute(address);
        } catch (ContractExecutorException e) {
            throw new TException(e.getMessage(), e);
        }
    }

    @Override
    public void execute(String address, String method, List<String> params) throws TException {
        String[] paramsArray = params == null ? null : params.toArray(new String[0]);
        try {
            service.execute(address, method, paramsArray);
        } catch (ContractExecutorException e) {
            throw new TException(e.getMessage(), e);
        }
    }
}
