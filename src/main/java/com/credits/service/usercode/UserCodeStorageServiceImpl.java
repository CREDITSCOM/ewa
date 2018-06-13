package com.credits.service.usercode;

import com.credits.classload.ClassPathLoader;
import com.credits.compilation.SimpleInMemoryCompiler;
import com.credits.exception.ClassLoadException;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Component
public class UserCodeStorageServiceImpl implements UserCodeStorageService {

    private final static String CLASS_EXT = "class";
    private final static String SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    @Resource
    private ClassPathLoader classPathLoader;

    @Resource
    private SimpleInMemoryCompiler compiler;

    @Override
    public void store(File file, String address) throws ContractExecutorException {
        String sourceFilePath = SOURCE_FOLDER_PATH + File.separator + address + File.separator + file.getName();
        File source = new File(sourceFilePath);
        source.getParentFile().mkdirs();

        try (InputStream is = new FileInputStream(file); OutputStream os = new FileOutputStream(source)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save the file " + file.getName() + ". Reason: "
                + e.getMessage(), e);
        }

        String ext = FilenameUtils.getExtension(source.getName());
        if (!CLASS_EXT.equalsIgnoreCase(ext)) {
            try {
                compiler.compile(source);
            } catch (CompilationException e) {
                source.delete();
                throw new ContractExecutorException("Cannot save the file " + file.getName() + ". Reason: "
                    + e.getMessage(), e);
            }
        }
    }

    @Override
    public Class<?> load(String address) throws ClassLoadException {
        File source = new File(SOURCE_FOLDER_PATH + File.separator + address);
        if (!source.exists()) {
            throw new ClassLoadException("File does not exist");
        }

        URL url;
        try {
            url = source.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ClassLoadException(e.getMessage(), e);
        }

        File[] files = source.listFiles();
        if (files == null || files.length == 0) {
            throw new ClassLoadException("File does not exist");
        }
        String fileName = files[0].getName();
        String className = FilenameUtils.getBaseName(fileName);
        Class<?> clazz = classPathLoader.loadClass(url, className);
        return clazz;
    }
}
