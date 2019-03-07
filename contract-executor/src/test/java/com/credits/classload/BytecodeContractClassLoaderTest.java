package com.credits.classload;

import com.credits.general.exception.CompilationErrorException;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationUnit;
import org.junit.Before;
import org.junit.Test;


public class BytecodeContractClassLoaderTest {

    String sourceCode;
    CompilationUnit compilationUnit;

    @Before
    public void setUp() throws Exception {
        sourceCode =
            "public class MySmartContract {\n" +
                "\n" +
                "    public MySmartContract() {\n" +
                "        System.out.println(\"Hello World!!\"); ;\n" +
                "    }\n" +
                "}";
        compilationUnit = InMemoryCompiler.compileSourceCode(sourceCode).getUnits().get(0);
    }

    @Test
    public void buildClassTest() throws Exception {
        Class clazz = new BytecodeContractClassLoader().loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
        clazz.newInstance();
    }

    @Test(expected = LinkageError.class)
    public void buildClassTwice() throws CompilationErrorException {
        BytecodeContractClassLoader loader = new BytecodeContractClassLoader();
        loader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
        loader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
    }

    @Test
    public void loadOtherClass() throws CompilationErrorException {
        sourceCode =
            "public class MySmartContract {\n" +
            "\n" +
            "    public MySmartContract() {\n" +
            "try {\n" +
            " new java.net.ServerSocket(5000);\n" +
            "} catch (java.io.IOException e) {\n" +
            "e.printStackTrace();\n" +
            "}\n" +
            "    }\n" +
            "}";

        compilationUnit = InMemoryCompiler.compileSourceCode(sourceCode).getUnits().get(0);
        BytecodeContractClassLoader loader = new BytecodeContractClassLoader();
        loader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
    }
}