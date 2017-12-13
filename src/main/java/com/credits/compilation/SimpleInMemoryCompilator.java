package com.credits.compilation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Component
public class SimpleInMemoryCompilator {

    private final static Logger logger = LoggerFactory.getLogger(SimpleInMemoryCompilator.class);

    public void compile(File source) {
        logger.debug("Compiling class {}", source.getName());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(source));

        CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);
        Boolean isCompiled = task.call();

        if (!isCompiled) {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics())
                logger.error("Error on line {} in {}. Message: {}", diagnostic.getLineNumber(), diagnostic.getSource(),
                        diagnostic.getMessage(null));
        }

        try {
            stdFileManager.close();
        } catch (IOException e) {
            logger.error("Cannot close file manager", e);
        }
    }
}
