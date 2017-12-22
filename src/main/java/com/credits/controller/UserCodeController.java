package com.credits.controller;

import com.credits.exception.ContractExecutorException;
import com.credits.service.StorageService;
import com.credits.vo.Transaction;
import org.springframework.web.bind.annotation.RequestBody;
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
    private StorageService storageService;

    //    curl -X POST -F 'java=@/path/to/java' -F 'address=1q2w3e4r' http://localhost:8080/submitJava
    @RequestMapping(method = RequestMethod.POST)
    public void doPost(@RequestParam("java") MultipartFile file,
                       @RequestParam("address") String address) throws ContractExecutorException {

        storageService.store(file, address);
    }

    //Testing DatabaseInteractionService
    @RequestMapping(value = "do", method = RequestMethod.GET)
    public Transaction[] doGetTest() {
        Transaction[] tr = {new Transaction("1", 23, '+'), new Transaction("2", 24)};
        return tr;
    }

    @RequestMapping(value = "do", method = RequestMethod.POST)
    public void doPostTest(@RequestBody Transaction transaction) {
        System.out.println(transaction);
    }
}
