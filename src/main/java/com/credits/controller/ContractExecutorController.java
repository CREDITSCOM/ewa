package com.credits.controller;

import com.credits.service.StorageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/executeContract")
public class ContractExecutorController {

//    @Resource
//    private StorageService storageService;

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(@RequestParam("address") String address) {
        /*TODO: check the address in the storage service and try to execute the java code hidden by this address.
          TODO: read the method name and the parameters which have to be passed to the method
          TODO: implement ContractExecutorService to be able to incapsulate the implementation of contract execution. This is the most important class that has to communicate with the LEVELDB CONNECTOR.
        */
    }
}
