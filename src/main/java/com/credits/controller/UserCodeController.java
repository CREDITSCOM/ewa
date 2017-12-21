package com.credits.controller;

import com.credits.exception.ContractExecutorException;
import com.credits.service.StorageService;
import com.credits.vo.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Controller
@RequestMapping("/submitJava")
public class UserCodeController {

    @Resource
    private StorageService storageService;

    @RequestMapping(method = RequestMethod.POST)
    public void doPost(@RequestParam("java") MultipartFile file,
                       @RequestParam("address") String address) throws ContractExecutorException {

        storageService.store(file, address);
    }

    //Testing DatabaseInteractionService
    @RequestMapping(value = "do", method = RequestMethod.GET)
    @ResponseBody
    public Transaction[] doGetTest() {
        Transaction[] tr = {new Transaction("1",23, '+'), new Transaction("2",24 )};
        return tr;
    }

    @RequestMapping(value = "do", method = RequestMethod.POST)
    public void doPostTest(@RequestBody Transaction transaction) {
        System.out.println(transaction);
    }
}
