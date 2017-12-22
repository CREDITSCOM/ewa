package com.credits.controller;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ContractExecutorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/executeContract")
public class ContractExecutorController {

    @Resource
    private ContractExecutorService contractExecutor;

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(@RequestParam("address") String address, @RequestParam("method") String methodName,
            @RequestParam(value = "params", required = false) String[] methodArgs) throws ContractExecutorException {

        contractExecutor.execute(address, methodName, methodArgs);
    }
}
