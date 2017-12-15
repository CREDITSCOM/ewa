package com.credits.controller;

import com.credits.exception.ContractExecutorException;
import com.credits.service.StorageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
}
