package com.credits.controller.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.ContractExecutorService;
import org.apache.commons.lang3.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/executeContract")
public class ContractExecutorController {

    @Resource
    private ContractExecutorService contractExecutor;

    //    curl -X GET 'http://localhost:8080/executeContract?address=1234qwer&method=foo&params=HelloWorld&params=123.11&params=321'
    @RequestMapping(method = RequestMethod.GET)
    public void doGet(@RequestParam("address") String address, @RequestParam("method") String methodName,
                      @RequestParam(value = "params", required = false) String[] methodArgs) throws ContractExecutorException {

        contractExecutor.execute(address);
    }
}
