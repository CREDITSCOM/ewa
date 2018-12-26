package com.credits.general.util.compiler;

import com.credits.general.exception.CompilationException;
import com.credits.general.util.PrintOut;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.compiler.model.CompilationUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import java.util.List;

public class InMemoryCompilerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCompilerTest.class);

    @Test
    public void compileTest01() {
        InMemoryCompiler compiler = new InMemoryCompiler();
        CompilationPackage compilationPackage = null;
        try {
            compilationPackage = compiler.compile("Contract",
                    "public class Contract extends SmartContract { \n" +
                            "public Contract() { \n" +
                            "total = 0;wqwe \n" +
                            "} \n" +
                            "}");
        } catch (CompilationException e) {
            e.printStackTrace();
        }
        if (compilationPackage.isCompilationStatusSuccess()) {
            List<CompilationUnit>  compilationUnits = compilationPackage.getUnits();
            CompilationUnit compilationUnit = compilationUnits.get(0);
            byte[] byteCode = compilationUnit.getBytecode();
            PrintOut.printBytes(byteCode);
        } else {
            DiagnosticCollector collector = compilationPackage.getCollector();
            List<Diagnostic> diagnostics = collector.getDiagnostics();
            diagnostics.forEach(action -> {
                LOGGER.info(String.format("Line number: %s; Error message: %s", action.getLineNumber(), action.getMessage(null)));
            });
        }

    }

    @Test
    public void loadJdkPathFromEnvironmentVariablesTest() {
        InMemoryCompiler compiler = new InMemoryCompiler();
        try {
            LOGGER.info(compiler.loadJdkPathFromEnvironmentVariables());
        } catch (CompilationException e) {
            e.printStackTrace();
        }
    }
}
