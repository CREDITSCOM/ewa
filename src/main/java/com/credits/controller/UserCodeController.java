package com.credits.controller;

import com.credits.compilation.SimpleInMemoryCompilator;
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

    private final static String CLASS_EXT = "class";

    @Resource
    private SimpleInMemoryCompilator compilator;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody String doGet() {
        return "Hello World";
    }

    @RequestMapping(method = RequestMethod.POST)
    public void doPost(@RequestParam("java") MultipartFile file) {
        File source = new File(file.getName());
        try (InputStream is = file.getInputStream(); OutputStream os = new FileOutputStream(source)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ext = FilenameUtils.getExtension(source.getName());
        if (CLASS_EXT.equalsIgnoreCase(ext)) {
            compilator.compile(source);
        }
    }
}
