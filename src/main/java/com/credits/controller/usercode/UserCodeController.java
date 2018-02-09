package com.credits.controller.usercode;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.usercode.UserCodeStorageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/submitJava")
public class UserCodeController {

    @Resource
    private UserCodeStorageService storageService;

    @Resource
    private ContractExecutorService executorService;

    //    curl -X POST -F 'java=@/path/to/java' -F 'address=1q2w3e4r' http://localhost:8080/submitJava
    @RequestMapping(method = RequestMethod.POST)
    public void doPost(@RequestParam("java") MultipartFile file,
                       @RequestParam("address") String address) throws ContractExecutorException {

        storageService.store(file, address);
        executorService.execute(address);
    }
}
