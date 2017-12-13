package com.credits.controller;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
@RequestMapping("/submitJava")
public class UserCodeController {

    @Resource
    private JavaStorageService storageService;

    @RequestMapping(method = RequestMapping.POST)
    public void doPost(@RequestParam("java") MultipartFile file) {
        storageService.store(file);
        //TODO: if java file is not compiled we have to compile it using SimpleInMemoryCompilator class and store with the original file
    }
}
