package com.credits.controller;

import com.credits.compilation.SimpleInMemoryCompilator;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.service.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;

@Controller
@RequestMapping("/submitJava")
public class UserCodeController {

    @Resource
    private StorageService storageService;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody String doGet() {
        return "Hello World";
    }

    @RequestMapping(method = RequestMethod.POST)
    public void doPost(@RequestParam("java") MultipartFile file,
                       @RequestParam("address") String address) throws ContractExecutorException {

        storageService.store(file, address);
    }
}
