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

    private final static String CLASS_EXT = "class";
    private final static String SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    @Resource
    private SimpleInMemoryCompilator compilator;

    @Resource
    private StorageService storageService;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody String doGet() {
        return "Hello World";
    }

    @RequestMapping(method = RequestMethod.POST)
    public void doPost(@RequestParam("java") MultipartFile file,
                       @RequestParam("address") String address,
                       @RequestParam("methodName") String methodName,
                       @RequestParam(value = "params", required = false) String params) throws ContractExecutorException {

        //TODO: move the logic of storing files to the service implementation

        String sourceFilePath = SOURCE_FOLDER_PATH + File.separator + address + File.separator + file.getName();
        File source = new File(sourceFilePath);
        source.getParentFile().mkdirs();

        try (InputStream is = file.getInputStream(); OutputStream os = new FileOutputStream(source)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save the file " + file.getName() + ". Reason: "
                + e.getMessage(), e);
        }

        String ext = FilenameUtils.getExtension(source.getName());
        if (CLASS_EXT.equalsIgnoreCase(ext)) {
            try {
                compilator.compile(source);
            } catch (CompilationException e) {
                source.delete();
                throw new ContractExecutorException("Cannot save the file " + file.getName() + ". Reason: "
                    + e.getMessage(), e);
            }
        }
    }
}
