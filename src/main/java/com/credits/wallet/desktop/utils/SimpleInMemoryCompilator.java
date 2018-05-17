package com.credits.wallet.desktop.utils;


import com.credits.wallet.desktop.exception.CompilationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class SimpleInMemoryCompilator {

    private final static Logger logger = LoggerFactory.getLogger(SimpleInMemoryCompilator.class);

    public static void compile(File source) throws CompilationException {
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
    }
}
