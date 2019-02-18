package com.credits.general.util.compiler;

import com.credits.general.exception.CompilationException;
import com.credits.general.util.PrintOut;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryCompilerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCompilerTest.class);

    @Test
    public void compileTest01() {
        InMemoryCompiler compiler = new InMemoryCompiler();
        CompilationPackage compilationPackage = null;
        try {
            String sourceCode =
                "public class Contract extends SmartContract { \n" + "public Contract() { \n" + "total = 0;wqwe \n" +
                    "} \n" + "}";
            Map<String,String> classesToCompile = new HashMap<>();
            String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
            classesToCompile.put(className,sourceCode);

            compilationPackage = compiler.compile(
                classesToCompile);
        } catch (CompilationException e) {
            e.printStackTrace();
        }
        if (compilationPackage.isCompilationStatusSuccess()) {
            List<CompilationUnit>  compilationUnits = compilationPackage.getUnits();
            CompilationUnit compilationUnit = compilationUnits.get(0);
            byte[] byteCode = compilationUnit.getByteCode();
            PrintOut.printBytes(byteCode);
        } else {
            DiagnosticCollector collector = compilationPackage.getCollector();
            List<Diagnostic> diagnostics = collector.getDiagnostics();
            diagnostics.forEach(action -> {
                Assert.assertEquals(action.getLineNumber(), 3);
            });
        }

    }

    @Test
    public void loadJdkPathFromEnvironmentVariablesTest() {
        InMemoryCompiler compiler = new InMemoryCompiler();
        try {
            Assert.assertNotNull(compiler.loadJdkPathFromEnvironmentVariables());
        } catch (CompilationException e) {
            e.printStackTrace();
        }
    }
}
