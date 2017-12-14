package com.credits.service;

import com.credits.compilation.SimpleInMemoryCompilator;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class UserCodeStorageService implements StorageService {

    private final static String CLASS_EXT = "class";
    private final static String SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    @Resource
    private SimpleInMemoryCompilator compilator;

    @Override
    public void init() {

    }

    @Override
    public void store(MultipartFile file, String address) throws ContractExecutorException {
        String sourceFilePath = SOURCE_FOLDER_PATH + File.separator + address + File.separator + file.getOriginalFilename();
        File source = new File(sourceFilePath);
        source.getParentFile().mkdirs();

        try (InputStream is = file.getInputStream(); OutputStream os = new FileOutputStream(source)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save the file " + file.getName() + ". Reason: "
                    + e.getMessage(), e);
        }

        String ext = FilenameUtils.getExtension(source.getName());
        if (!CLASS_EXT.equalsIgnoreCase(ext)) {
            try {
                compilator.compile(source);
            } catch (CompilationException e) {
                source.delete();
                throw new ContractExecutorException("Cannot save the file " + file.getName() + ". Reason: "
                        + e.getMessage(), e);
            }
        }
    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public Path load(String filename) {
        return null;
    }

    @Override
    public org.springframework.core.io.Resource loadAsResource(String filename) {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
