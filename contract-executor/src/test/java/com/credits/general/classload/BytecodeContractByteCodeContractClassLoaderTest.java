package com.credits.general.classload;

import com.credits.general.util.compiler.CompilationException;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationUnit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class BytecodeContractByteCodeContractClassLoaderTest {

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
        Class clazz = new ByteCodeContractClassLoader().loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
        assertNotNull(clazz.newInstance());
    }

    @Test
    public void buildClassTwice() {
        ByteCodeContractClassLoader loader = new ByteCodeContractClassLoader();
        loader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
        loader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
    }

    @Test
    public void loadOtherClass() throws CompilationException {
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
        ByteCodeContractClassLoader loader = new ByteCodeContractClassLoader();
        loader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
    }
}