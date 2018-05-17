package com.credits.wallet.desktop.utils;


import com.credits.wallet.desktop.exception.CompilationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class SimpleInMemoryCompilator {

    private final static Logger logger = LoggerFactory.getLogger(SimpleInMemoryCompilator.class);
    private final static String SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "temp" + File.separator;

    public static byte[] compile(String sourceString, String classname, String token) throws CompilationException {
        File sourceFolder = new File(SOURCE_FOLDER_PATH + token);

        File source = save(sourceFolder, classname, sourceString);

        logger.debug("Compiling class {}", source.getName());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(source));

        CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);
        Boolean isCompiled = task.call();

        if (!isCompiled) {
            StringBuilder errorMessage = new StringBuilder("");
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                logger.error("Error on line {} in {}. Message: {}", diagnostic.getLineNumber(), diagnostic.getSource(),
                    diagnostic.getMessage(null));
                errorMessage.append(String.format("Error on line %d. Message: %s\n", diagnostic.getLineNumber(),
                    diagnostic.getMessage(null)));
            }
            throw new CompilationException("Cannot compile the file: " + source.getName() + "\n" + errorMessage.toString());
        }

        try {
            stdFileManager.close();
        } catch (IOException e) {
            logger.error("Cannot close file manager", e);
        }

        byte[] sourceBytes;
        try {
            File classFile = new File(sourceFolder + File.separator + classname + ".class");
            sourceBytes = FileUtils.readFileToByteArray(classFile);
        } catch (IOException e) {
            throw new CompilationException("Cannot read bytes from source file.", e);
        }

        for (File file : sourceFolder.listFiles()) {
            file.delete();
        }
        sourceFolder.delete();

        return sourceBytes;

    }

    private static File save(File sourceFolder, String classname, String sourceString) throws CompilationException {
        byte[] sourceBytes = sourceString.getBytes();
        File sourceFile = new File(sourceFolder + File.separator + classname + ".java");
        try {
            FileUtils.writeByteArrayToFile(sourceFile, sourceBytes);
        } catch (IOException e) {
            throw new CompilationException("Cannot save source to file.", e);
        }

        return sourceFile;
    }
}
